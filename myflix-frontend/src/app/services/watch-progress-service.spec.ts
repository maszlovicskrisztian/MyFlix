import { TestBed } from '@angular/core/testing';

import { WatchProgressService } from './watch-progress-service';

describe('WatchProgressService', () => {
  let service: WatchProgressService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WatchProgressService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
