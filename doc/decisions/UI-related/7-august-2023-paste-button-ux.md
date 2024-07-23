### How should the paste button behave when clipboard is empty?

This question first came up as a review comment to this PR  
https://github.com/status-im/status-mobile/pull/16852

There were 2 considerations : 
- Either keep the paste button disabled when there is nothing in the clipboard OR
- Always keep the paste button enabled 

There were positives and negatives for both approaches.

Positives of keeping paste button disabled when there is nothing in clipboard would require us to 
check the value of clipboard as soon as the component is mounted (i.e when the user first sees
the screen). In iOS this means a native permissions dialog would appear requesting for permissions
to paste from the clipboard.

Negatives of this approach is that as soon as any user navigates to this screen they are greeted with 
this popup which can be annoying sometimes.

Positives of keeping paste button always enabled is that we can trigger a request to the clipboard on
tap of the paste button which would trigger the native permissions dialog requesting for permissions 
to paste from the clipboard.
In this case seeing this dialog is okay because the user has initiated a paste action.

Negatives of this approach is that in the event the clipboard is empty the user will still see the 
system dialog and on approving nothing will be pasted (because the clipboard was empty).
This behaviour can be confusing.

On consulting the Design Team via discord it was concluded that out of the two approaches
having the paste button always enabled is a better UX overall.

