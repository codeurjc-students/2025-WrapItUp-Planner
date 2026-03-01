import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { NoteDetailComponent } from './components/notes-details.component';
import { CreateNoteComponent } from './components/create-note.component';
import { NoteService } from './services/note.service';
import { AuthComponent } from './components/auth/auth.component';
import { AuthService } from './services/auth.service';
import { routes } from './app.routes';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HeaderComponent } from './components/header/header.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { ProfileComponent } from './components/profile/profile.component';
import { MyNotesComponent } from './components/my-notes/my-notes.component';
import { ReportedCommentsComponent } from './components/reported-comments/reported-comments.component';
import { BannedComponent } from './components/banned/banned.component';
import { CalendarComponent } from './components/calendar/calendar.component';
import { DayViewDialogComponent } from './components/calendar/day-view-dialog.component';
import { CreateEventDialogComponent } from './components/calendar/create-event-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';


@NgModule({
  declarations: [
    AppComponent,
    NoteDetailComponent,
    CreateNoteComponent,
    AuthComponent,
    LandingPageComponent,
    HeaderComponent,
    AboutUsComponent,
    ProfileComponent,
    MyNotesComponent,
    ReportedCommentsComponent,
    BannedComponent,
    CalendarComponent,
    DayViewDialogComponent,
    CreateEventDialogComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    MatToolbarModule,
    MatButtonModule,
    BrowserAnimationsModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  providers: [NoteService, AuthService, provideAnimationsAsync()],
  bootstrap: [AppComponent]
})
export class AppModule { }