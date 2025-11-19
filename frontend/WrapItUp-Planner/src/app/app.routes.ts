import { RouterModule, Routes } from '@angular/router';
import { AINoteDetailComponent } from './components/notes-details.component';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthComponent } from './components/auth/auth.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { AboutUsComponent } from './components/about-us/about-us.component';


export const routes: Routes = [
    { path: 'notes/:id', component: AINoteDetailComponent }
  , { path: 'login', component: AuthComponent } 
  , { path: 'register', component: AuthComponent }
  , { path: '', component: LandingPageComponent }
  , { path: 'about-us', component: AboutUsComponent }
]; 

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule, FormsModule]
})
export class AppRoutingModule { }