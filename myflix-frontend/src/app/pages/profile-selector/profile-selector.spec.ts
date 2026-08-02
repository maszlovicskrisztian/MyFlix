import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ProfileSelector } from './profile-selector';

describe('ProfileSelector', () => {
  let component: ProfileSelector;
  let fixture: ComponentFixture<ProfileSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileSelector],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
