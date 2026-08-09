import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowDetails } from './show-details';

describe('ShowDetails', () => {
  let component: ShowDetails;
  let fixture: ComponentFixture<ShowDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShowDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(ShowDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
