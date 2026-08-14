import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MediaSearchDialog } from './media-search-dialog';

describe('MediaSearchDialog', () => {
  let component: MediaSearchDialog;
  let fixture: ComponentFixture<MediaSearchDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MediaSearchDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(MediaSearchDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
