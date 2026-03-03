import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <h1>Motor Financeiro</h1>
    <router-outlet />
  `,
  styles: [`
    h1 {
      padding: 1rem;
      color: #1e40af;
    }
  `]
})
export class AppComponent {
  title = 'Motor Financeiro';
}
