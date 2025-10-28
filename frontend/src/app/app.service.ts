import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Statistics {
  totalProjects: number;
  totalLinesOfCode: number;
  maxLinesOfCode: number;
  percentageUsage: number;
  projectsToDelete: number;
}

export interface CleanupResult {
  projectsDeleted: number;
  linesOfCodeDeleted: number;
  linesOfCodeRemaining: number;
  status: string;
  message: string;
}

export interface Project {
  projectKey: string;
  projectName: string;
  linesOfCode: number;
  lastScanDate: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppService {
  private apiUrl = 'http://localhost:6996/api';

  constructor(private http: HttpClient) {}

  getStatistics(): Observable<Statistics> {
    return this.http.get<Statistics>(`${this.apiUrl}/statistics`);
  }

  executeCleanup(): Observable<CleanupResult> {
    return this.http.post<CleanupResult>(`${this.apiUrl}/cleanup/execute`, {});
  }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/projects`);
  }

  syncProjects(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/projects/sync`, {});
  }
}

