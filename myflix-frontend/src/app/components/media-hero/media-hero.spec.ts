import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MediaHero } from './media-hero';

describe('MediaHero', () => {
  let component: MediaHero;
  let fixture: ComponentFixture<MediaHero>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MediaHero],
    }).compileComponents();

    fixture = TestBed.createComponent(MediaHero);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
