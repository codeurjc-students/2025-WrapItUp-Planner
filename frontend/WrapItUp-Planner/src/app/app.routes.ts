import { RouterModule, Routes } from '@angular/router';
import { AINoteDetailComponent } from './components/notes-details.component';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from './components/auth/login.component';
import { RegisterComponent } from './components/auth/register.component';


export const routes: Routes = [
    { path: 'notes/:id', component: AINoteDetailComponent }
  , { path: 'login', component: LoginComponent }
  , { path: 'register', component: RegisterComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule, FormsModule]
})
export class AppRoutingModule { }