import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { MediaPlayer } from './media-player';

describe('MediaPlayer', () => {
  let component: MediaPlayer;
  let fixture: ComponentFixture<MediaPlayer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MediaPlayer],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MediaPlayer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
