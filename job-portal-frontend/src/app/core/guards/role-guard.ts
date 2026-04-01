import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const expectedRoles: string[] = route.data['roles'];
  const userRole = authService.getRole();

  if (userRole && expectedRoles.includes(userRole)) {
    return true;
  }

  router.navigate(['/jobs']);
  return false;
};