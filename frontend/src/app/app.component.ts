import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppService, Statistics, CleanupResult } from './app.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  statistics: Statistics | null = null;
  loading = false;
  cleanupLoading = false;
  lastCleanupResult: CleanupResult | null = null;
  error: string | null = null;

  constructor(private appService: AppService) {}

  ngOnInit() {
    this.loadStatistics();
  }

  loadStatistics() {
    this.loading = true;
    this.error = null;
    
    this.appService.getStatistics().subscribe({
      next: (data) => {
        this.statistics = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load statistics. Please ensure the backend is running.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  executeCleanup() {
    if (!confirm('Are you sure you want to execute the cleanup? This will delete projects older than 14 days.')) {
      return;
    }

    this.cleanupLoading = true;
    this.error = null;
    
    this.appService.executeCleanup().subscribe({
      next: (result) => {
        this.lastCleanupResult = result;
        this.cleanupLoading = false;
        this.loadStatistics();
        alert(`Cleanup completed: ${result.message}`);
      },
      error: (err) => {
        this.error = 'Failed to execute cleanup.';
        this.cleanupLoading = false;
        console.error(err);
        alert('Cleanup failed. Please check the logs.');
      }
    });
  }

  syncProjects() {
    this.loading = true;
    this.appService.syncProjects().subscribe({
      next: () => {
        this.loadStatistics();
      },
      error: (err) => {
        this.error = 'Failed to sync projects.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  formatNumber(num: number): string {
    return num.toLocaleString();
  }
}

