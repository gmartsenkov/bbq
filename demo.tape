# vhs script: generate assets/demo.gif
# Run with: vhs demo.tape   (from ~/Development/http-bb)
#
# Assumes `httpee` is on $PATH (via `bbin install ~/Development/http-bb`)
# and that `bat` is installed for body syntax highlighting.

Output assets/demo.gif

Set Shell "zsh"
Set FontFamily "JetBrainsMono Nerd Font Mono"
Set FontSize 20
Set Width 1100
Set Height 1000
Set Theme "Catppuccin Mocha"
Set Padding 20
Set TypingSpeed 120ms

# Setup: cd into the example project so httpee finds its httpee.edn,
# set BOB (referenced by users/show), and clear the screen.
Hide
Type "cd examples && export BOB=demo && clear"
Enter
Sleep 300ms
Show

# 1. Listing templates surfaces titles from each request map.
Type "httpee"
Sleep 500ms
Enter
Sleep 2500ms

Type "clear"
Enter
Sleep 200ms

# 2. Run a template with an override.
Type "httpee users/show id=42"
Sleep 500ms
Enter
Sleep 4500ms

Type "clear"
Enter
Sleep 200ms
