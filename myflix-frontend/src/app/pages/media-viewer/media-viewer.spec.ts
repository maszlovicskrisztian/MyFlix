import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { MediaViewer } from './media-viewer';

describe('MediaViewer', () => {
  let component: MediaViewer;
  let fixture: ComponentFixture<MediaViewer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MediaViewer],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MediaViewer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
