import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatUiComponent } from './chat-ui.component';

describe('ChatUiComponent', () => {
  let component: ChatUiComponent;
  let fixture: ComponentFixture<ChatUiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatUiComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChatUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
