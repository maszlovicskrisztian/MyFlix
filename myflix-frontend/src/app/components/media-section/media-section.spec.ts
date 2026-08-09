import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MediaSection } from './media-section';

describe('MediaSection', () => {
  let component: MediaSection;
  let fixture: ComponentFixture<MediaSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MediaSection],
    }).compileComponents();

    fixture = TestBed.createComponent(MediaSection);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
