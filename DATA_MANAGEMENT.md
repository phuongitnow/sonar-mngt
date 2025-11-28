# Tài liệu Quản lý Dữ liệu - SonarQube Admin Application

## 📋 Mục lục

1. [Tổng quan](#tổng-quan)
2. [Database Schema](#database-schema)
3. [Entities và Relationships](#entities-và-relationships)
4. [Data Flow](#data-flow)
5. [Database Migration](#database-migration)
6. [Data Synchronization](#data-synchronization)
7. [Data Retention](#data-retention)
8. [Backup và Recovery](#backup-và-recovery)
9. [Performance Optimization](#performance-optimization)

---

## Tổng quan

SonarQube Admin Application sử dụng **PostgreSQL** làm database chính để lưu trữ:
- Thông tin snapshot của các dự án SonarQube
- Lịch sử các lần thực thi cleanup
- Metadata và timestamps

### Database Technology Stack

- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA / Hibernate
- **Migration Tool**: Liquibase 4.25.1
- **Connection Pooling**: HikariCP (default Spring Boot)

---

## Database Schema

### Schema Overview

```
sonarqube_admin (Database)
│
├── project_snapshots (Table)
│   ├── id (BIGSERIAL, PK)
│   ├── project_key (VARCHAR(255), UNIQUE, NOT NULL)
│   ├── project_name (VARCHAR(500), NOT NULL)
│   ├── lines_of_code (BIGINT)
│   ├── last_scan_date (TIMESTAMP)
│   ├── created_at (TIMESTAMP)
│   └── updated_at (TIMESTAMP)
│
└── cleanup_history (Table)
    ├── id (BIGSERIAL, PK)
    ├── executed_at (TIMESTAMP)
    ├── projects_deleted (INTEGER)
    ├── lines_of_code_deleted (BIGINT)
    ├── lines_of_code_remaining (BIGINT)
    ├── status (VARCHAR(50))
    └── error_message (VARCHAR(5000))
```

### Table: project_snapshots

Lưu trữ snapshot của các dự án SonarQube, được đồng bộ định kỳ từ SonarQube server.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY, NOT NULL | Auto-increment ID |
| `project_key` | VARCHAR(255) | UNIQUE, NOT NULL | SonarQube project key (unique identifier) |
| `project_name` | VARCHAR(500) | NOT NULL | Tên dự án |
| `lines_of_code` | BIGINT | NULL | Tổng số dòng code (LOC) |
| `last_scan_date` | TIMESTAMP | NULL | Ngày scan cuối cùng |
| `created_at` | TIMESTAMP | NULL | Thời gian tạo record |
| `updated_at` | TIMESTAMP | NULL | Thời gian cập nhật cuối |

**Indexes**:
- `idx_project_snapshots_project_key` trên `project_key` (unique)
- `idx_project_snapshots_last_scan_date` trên `last_scan_date`

**Business Rules**:
- `project_key` phải unique (một project chỉ có một snapshot)
- `created_at` và `updated_at` tự động set bởi JPA `@PrePersist` và `@PreUpdate`
- `lines_of_code` có thể NULL nếu SonarQube không trả về metric

### Table: cleanup_history

Lưu trữ lịch sử các lần thực thi cleanup process.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY, NOT NULL | Auto-increment ID |
| `executed_at` | TIMESTAMP | NULL | Thời gian thực thi cleanup |
| `projects_deleted` | INTEGER | NULL | Số lượng project đã xóa |
| `lines_of_code_deleted` | BIGINT | NULL | Tổng LOC đã xóa |
| `lines_of_code_remaining` | BIGINT | NULL | LOC còn lại sau cleanup |
| `status` | VARCHAR(50) | NULL | Trạng thái: RUNNING, COMPLETED, FAILED, SKIPPED |
| `error_message` | VARCHAR(5000) | NULL | Thông báo lỗi (nếu có) |

**Indexes**:
- `idx_cleanup_history_executed_at` trên `executed_at`

**Status Values**:
- `RUNNING`: Cleanup đang chạy
- `COMPLETED`: Cleanup hoàn thành thành công
- `FAILED`: Cleanup thất bại
- `SKIPPED`: Cleanup bị bỏ qua (LOC dưới limit)

---

## Entities và Relationships

### Entity: ProjectSnapshot

**Package**: `com.sonarqube.adminapp.domain.entity`

```java
@Entity
@Table(name = "project_snapshots")
public class ProjectSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String projectKey;
    
    @Column(nullable = false)
    private String projectName;
    
    @Column
    private Long linesOfCode;
    
    @Column
    private LocalDateTime lastScanDate;
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Lifecycle**:
1. Tạo mới khi sync project từ SonarQube
2. Cập nhật khi sync lại (update LOC, lastScanDate)
3. Xóa khi project bị xóa khỏi SonarQube

### Entity: CleanupHistory

**Package**: `com.sonarqube.adminapp.domain.entity`

```java
@Entity
@Table(name = "cleanup_history")
public class CleanupHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private LocalDateTime executedAt;
    
    @Column
    private Integer projectsDeleted;
    
    @Column
    private Long linesOfCodeDeleted;
    
    @Column
    private Long linesOfCodeRemaining;
    
    @Column
    private String status;
    
    @Column(length = 5000)
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
}
```

**Lifecycle**:
1. Tạo record với status `RUNNING` khi bắt đầu cleanup
2. Cập nhật với kết quả khi hoàn thành
3. Record được giữ lại để audit (không xóa)

### Relationships

Hiện tại, hai bảng **không có foreign key relationship** trực tiếp. Tuy nhiên, có mối quan hệ logic:

- `cleanup_history.projects_deleted` → Số lượng records trong `project_snapshots` đã bị xóa
- `cleanup_history.lines_of_code_deleted` → Tổng LOC từ các `project_snapshots` đã xóa

**Lý do không có FK**:
- Projects bị xóa khỏi database khi cleanup
- CleanupHistory cần giữ lại thông tin audit ngay cả khi projects đã bị xóa

---

## Data Flow

### 1. Sync Projects Flow

```
┌─────────────┐
│ SonarQube   │
│   Server    │
└──────┬──────┘
       │
       │ 1. GET /api/projects/search
       │ 2. GET /api/measures/component (LOC)
       │ 3. GET /api/project_analyses/search (last scan)
       │
       ▼
┌─────────────────────────────────────┐
│  SonarQubeServiceImpl               │
│  - getAllProjects()                 │
│  - getLinesOfCode()                 │
│  - getLastScanDate()                │
└──────────────┬──────────────────────┘
               │
               │ 4. updateProjectSnapshot()
               │
               ▼
┌─────────────────────────────────────┐
│  ProjectSnapshotRepository          │
│  - findByProjectKey()               │
│  - save()                           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  PostgreSQL Database                 │
│  project_snapshots table             │
│  - INSERT (new project)              │
│  - UPDATE (existing project)         │
└─────────────────────────────────────┘
```

**Data Operations**:
- **INSERT**: Khi project mới được tìm thấy trong SonarQube
- **UPDATE**: Khi project đã tồn tại (cập nhật LOC, lastScanDate)
- **UPSERT Logic**: 
  ```java
  ProjectSnapshot existing = repository.findByProjectKey(key)
      .orElse(ProjectSnapshot.builder().projectKey(key).build());
  // Update fields
  repository.save(existing);
  ```

### 2. Cleanup Flow

```
┌─────────────────────────────────────┐
│  CleanupService.performCleanup()    │
└──────────────┬──────────────────────┘
               │
               │ 1. syncProjects()
               │    (Update latest data)
               │
               ▼
┌─────────────────────────────────────┐
│  Calculate Statistics                │
│  - getTotalLinesOfCode()            │
│  - findProjectsOlderThan()          │
└──────────────┬──────────────────────┘
               │
               │ 2. Check if cleanup needed
               │    (LOC > 1,000,000?)
               │
               ▼
┌─────────────────────────────────────┐
│  For each old project:               │
│  - sonarQubeService.deleteProject() │
│  - projectSnapshotRepository.delete()│
└──────────────┬──────────────────────┘
               │
               │ 3. Save cleanup history
               │
               ▼
┌─────────────────────────────────────┐
│  CleanupHistoryRepository.save()    │
│  - executedAt                       │
│  - projectsDeleted                  │
│  - linesOfCodeDeleted               │
│  - status                           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  PostgreSQL Database                 │
│  - cleanup_history: INSERT          │
│  - project_snapshots: DELETE         │
└─────────────────────────────────────┘
```

**Data Operations**:
- **DELETE**: Xóa records trong `project_snapshots` khi project bị xóa
- **INSERT**: Tạo record mới trong `cleanup_history` sau mỗi lần cleanup

### 3. Statistics Flow

```
┌─────────────────────────────────────┐
│  StatisticsController                │
│  GET /api/statistics                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  CleanupService.getStatistics()    │
│  - projectSnapshotRepository.count()│
│  - getTotalLinesOfCode()            │
│  - findProjectsOlderThan()          │
└──────────────┬──────────────────────┘
               │
               │ SQL Queries:
               │ 1. SELECT COUNT(*) FROM project_snapshots
               │ 2. SELECT SUM(lines_of_code) FROM project_snapshots
               │ 3. SELECT * FROM project_snapshots 
               │    WHERE last_scan_date < threshold
               │
               ▼
┌─────────────────────────────────────┐
│  PostgreSQL Database                 │
│  - Aggregation queries               │
│  - Filter queries                    │
└─────────────────────────────────────┘
```

---

## Database Migration

### Liquibase Configuration

**Migration Tool**: Liquibase 4.25.1

**Location**: `backend/src/main/resources/db/changelog/`

**Structure**:
```
db/
└── changelog/
    ├── db.changelog-master.xml          # Master changelog
    └── changes/
        ├── v001-create-project-snapshots-table.xml
        └── v002-create-cleanup-history-table.xml
```

### Master Changelog

```xml
<databaseChangeLog>
    <include file="db/changelog/changes/v001-create-project-snapshots-table.xml"/>
    <include file="db/changelog/changes/v002-create-cleanup-history-table.xml"/>
</databaseChangeLog>
```

### Migration Scripts

#### v001: Create project_snapshots table

```xml
<changeSet id="1" author="admin">
    <createTable tableName="project_snapshots">
        <column name="id" type="BIGSERIAL">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="project_key" type="VARCHAR(255)">
            <constraints nullable="false" unique="true"/>
        </column>
        <!-- ... other columns ... -->
    </createTable>
    
    <createIndex indexName="idx_project_snapshots_project_key" 
                 tableName="project_snapshots">
        <column name="project_key"/>
    </createIndex>
</changeSet>
```

#### v002: Create cleanup_history table

```xml
<changeSet id="2" author="admin">
    <createTable tableName="cleanup_history">
        <!-- ... columns ... -->
    </createTable>
    
    <createIndex indexName="idx_cleanup_history_executed_at" 
                 tableName="cleanup_history">
        <column name="executed_at"/>
    </createIndex>
</changeSet>
```

### Migration Execution

Liquibase tự động chạy khi Spring Boot khởi động:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
```

**Process**:
1. Spring Boot khởi động
2. Liquibase kiểm tra `databasechangelog` table
3. Chạy các changesets chưa được apply
4. Ghi lại vào `databasechangelog` table

### Best Practices

- ✅ Mỗi changeset có ID và author duy nhất
- ✅ Changesets là idempotent (có thể chạy nhiều lần)
- ✅ Sử dụng rollback khi có thể
- ✅ Test migrations trên staging trước

---

## Data Synchronization

### Sync Strategy

**Trigger**: Manual (API) hoặc Automatic (scheduled)

**Frequency**:
- Manual: Khi user click "Sync Projects"
- Automatic: Trước mỗi lần cleanup

**Process**:

1. **Fetch từ SonarQube**:
   - Lấy danh sách tất cả projects (pagination)
   - Với mỗi project:
     - Lấy LOC metric
     - Lấy last scan date

2. **Update Database**:
   - Tìm project theo `project_key`
   - Nếu tồn tại: UPDATE
   - Nếu không: INSERT

3. **Cleanup Orphaned Records**:
   - Hiện tại: Không tự động xóa projects không còn trong SonarQube
   - Có thể thêm: Xóa projects không còn trong SonarQube sau N ngày

### Sync Implementation

```java
public void syncProjects() {
    List<ProjectDTO> projects = getAllProjects(); // From SonarQube
    
    for (ProjectDTO project : projects) {
        ProjectSnapshot existing = repository.findByProjectKey(project.getProjectKey())
            .orElse(ProjectSnapshot.builder()
                .projectKey(project.getProjectKey())
                .build());
        
        existing.setProjectName(project.getProjectName());
        existing.setLinesOfCode(project.getLinesOfCode());
        existing.setLastScanDate(project.getLastScanDate());
        
        repository.save(existing);
    }
}
```

### Data Consistency

**Challenges**:
- SonarQube có thể thay đổi project key/name
- Projects có thể bị xóa khỏi SonarQube
- Network issues khi sync

**Solutions**:
- ✅ Unique constraint trên `project_key`
- ✅ Timestamp tracking (`updated_at`)
- ⚠️ Cần thêm: Cleanup orphaned records
- ⚠️ Cần thêm: Retry mechanism cho failed syncs

---

## Data Retention

### Cleanup History Retention

**Policy**: Giữ lại tất cả cleanup history records (không tự động xóa)

**Lý do**:
- Audit trail
- Compliance
- Debugging và troubleshooting

**Recommendation**: 
- Có thể thêm scheduled job để archive old records (> 1 year)
- Hoặc xóa records cũ hơn N tháng (configurable)

### Project Snapshots Retention

**Policy**: 
- Projects được giữ lại cho đến khi bị xóa bởi cleanup process
- Cleanup xóa projects cũ hơn `DAYS_THRESHOLD` (mặc định: 14 ngày)

**Cleanup Criteria**:
1. `last_scan_date < (NOW() - DAYS_THRESHOLD)`
2. Tổng LOC > 1,000,000
3. Xóa cho đến khi LOC < 1,000,000 + buffer (100,000)

### Data Archival Strategy

**Hiện tại**: Không có archival

**Recommendation**:
- Archive cleanup_history cũ hơn 1 năm vào separate table
- Archive project_snapshots đã bị xóa vào `project_snapshots_archive`

---

## Backup và Recovery

### Backup Strategy

#### 1. Database Backup

**PostgreSQL Backup**:

```bash
# Full backup
pg_dump -U admin -d sonarqube_admin > backup_$(date +%Y%m%d).sql

# Backup với compression
pg_dump -U admin -d sonarqube_admin | gzip > backup_$(date +%Y%m%d).sql.gz
```

**Automated Backup** (Docker):

```bash
# Backup volume
docker run --rm \
  -v sonarqube-admin_postgres_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/postgres_backup_$(date +%Y%m%d).tar.gz /data
```

#### 2. Volume Backup

Docker Compose volume: `postgres_data`

```bash
# Backup volume
docker run --rm \
  -v sonarqube-admin_postgres_data:/source:ro \
  -v $(pwd):/backup \
  alpine tar czf /backup/postgres_volume_$(date +%Y%m%d).tar.gz -C /source .
```

### Recovery Strategy

#### 1. Restore từ SQL dump

```bash
# Restore
psql -U admin -d sonarqube_admin < backup_20240101.sql
```

#### 2. Restore từ volume backup

```bash
# Stop container
docker-compose down

# Restore volume
docker run --rm \
  -v sonarqube-admin_postgres_data:/target \
  -v $(pwd):/backup \
  alpine sh -c "cd /target && rm -rf * && tar xzf /backup/postgres_volume_20240101.tar.gz"

# Start container
docker-compose up -d
```

### Backup Schedule Recommendation

- **Daily**: Full backup (giữ 7 ngày)
- **Weekly**: Full backup (giữ 4 tuần)
- **Monthly**: Full backup (giữ 12 tháng)

### Disaster Recovery Plan

1. **RTO (Recovery Time Objective)**: < 4 giờ
2. **RPO (Recovery Point Objective)**: < 24 giờ (daily backup)

**Steps**:
1. Identify failure
2. Restore từ backup gần nhất
3. Verify data integrity
4. Restart services
5. Sync projects từ SonarQube (nếu cần)

---

## Performance Optimization

### Indexes

**Current Indexes**:
- `idx_project_snapshots_project_key` (unique)
- `idx_project_snapshots_last_scan_date`
- `idx_cleanup_history_executed_at`

**Additional Recommendations**:

```sql
-- Index for statistics queries
CREATE INDEX idx_project_snapshots_lines_of_code 
ON project_snapshots(lines_of_code) 
WHERE lines_of_code IS NOT NULL;

-- Index for cleanup queries (composite)
CREATE INDEX idx_project_snapshots_cleanup 
ON project_snapshots(last_scan_date, lines_of_code) 
WHERE last_scan_date IS NOT NULL;
```

### Query Optimization

**1. Statistics Query**:

```java
// Current: Aggregation query
@Query("SELECT SUM(p.linesOfCode) FROM ProjectSnapshot p")
Long getTotalLinesOfCode();
```

**Optimization**:
- Index trên `lines_of_code`
- Consider materialized view nếu query thường xuyên

**2. Find Old Projects Query**:

```java
// Current: Filter by date
@Query("SELECT p FROM ProjectSnapshot p WHERE p.lastScanDate < :thresholdDate")
List<ProjectSnapshot> findProjectsOlderThan(LocalDateTime thresholdDate);
```

**Optimization**:
- Index trên `last_scan_date`
- Limit results nếu không cần tất cả

### Connection Pooling

**Default**: HikariCP (Spring Boot)

**Configuration** (application.yml):

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Caching Strategy

**Current**: Không có caching

**Recommendations**:
- Cache statistics (TTL: 5 phút)
- Cache project list (TTL: 10 phút)
- Invalidate cache khi sync/cleanup

**Example với Spring Cache**:

```java
@Cacheable(value = "statistics", unless = "#result == null")
public StatisticsDTO getStatistics() {
    // ...
}

@CacheEvict(value = "statistics", allEntries = true)
public void syncProjects() {
    // ...
}
```

### Database Maintenance

**Vacuum và Analyze**:

```sql
-- Manual vacuum
VACUUM ANALYZE project_snapshots;
VACUUM ANALYZE cleanup_history;

-- Auto-vacuum (PostgreSQL default)
-- Configure trong postgresql.conf
```

**Monitoring**:
- Table sizes
- Index usage
- Query performance
- Connection pool stats

---

## Data Validation và Constraints

### Database Constraints

1. **Primary Keys**: `id` (auto-increment)
2. **Unique Constraints**: `project_key` trong `project_snapshots`
3. **NOT NULL**: `project_key`, `project_name`
4. **Check Constraints**: Có thể thêm (ví dụ: `lines_of_code >= 0`)

### Application-Level Validation

1. **Entity Validation**: JPA annotations
2. **DTO Validation**: Spring Validation
3. **Business Rules**: Service layer

### Data Integrity

**Referential Integrity**: 
- Không có foreign keys (by design)
- Application-level consistency

**Data Quality**:
- Validate data từ SonarQube API
- Handle NULL values appropriately
- Log data inconsistencies

---

## Monitoring và Alerting

### Key Metrics

1. **Database Size**: Monitor growth
2. **Table Sizes**: `project_snapshots`, `cleanup_history`
3. **Query Performance**: Slow queries
4. **Connection Pool**: Active/idle connections
5. **Sync Frequency**: Thời gian giữa các lần sync
6. **Cleanup Frequency**: Số lần cleanup per day/week

### Recommended Queries

```sql
-- Database size
SELECT pg_size_pretty(pg_database_size('sonarqube_admin'));

-- Table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Row counts
SELECT 
    'project_snapshots' AS table_name,
    COUNT(*) AS row_count
FROM project_snapshots
UNION ALL
SELECT 
    'cleanup_history' AS table_name,
    COUNT(*) AS row_count
FROM cleanup_history;

-- Recent cleanups
SELECT * FROM cleanup_history 
ORDER BY executed_at DESC 
LIMIT 10;
```

---

## Kết luận

Hệ thống quản lý dữ liệu của SonarQube Admin Application được thiết kế với:

- ✅ **Simple Schema**: 2 bảng chính, dễ quản lý
- ✅ **Audit Trail**: Lưu lại lịch sử cleanup
- ✅ **Migration Management**: Liquibase cho version control
- ✅ **Data Synchronization**: Tự động sync từ SonarQube
- ✅ **Performance**: Indexes cho các queries quan trọng

**Cải thiện đề xuất**:
- [ ] Thêm data archival strategy
- [ ] Implement caching cho statistics
- [ ] Thêm monitoring và alerting
- [ ] Automated backup strategy
- [ ] Data validation và quality checks


