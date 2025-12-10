import { RouterModule, Routes } from '@angular/router';
import { NoteDetailComponent } from './components/notes-details.component';
import { CreateNoteComponent } from './components/create-note.component';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthComponent } from './components/auth/auth.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AuthGuard } from './guards/auth.guard';
import { MyNotesComponent } from './components/my-notes/my-notes.component';
import { ErrorPageComponent } from './components/error-page/error-page.component';


export const routes: Routes = [
    { path: 'notes/create', component: CreateNoteComponent, canActivate: [AuthGuard] },
    { path: 'notes/:id', component: NoteDetailComponent },
    { path: 'my-notes', component: MyNotesComponent, canActivate: [AuthGuard] }
  , { path: 'login', component: AuthComponent } 
  , { path: 'register', component: AuthComponent }
  , { path: '', component: LandingPageComponent }
  , { path: 'about-us', component: AboutUsComponent }
  , { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] }
  , { path: 'error', component: ErrorPageComponent }
  , { path: '**', redirectTo: '/' }
]; 

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule, FormsModule]
})
export class AppRoutingModule { }