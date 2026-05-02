#!/usr/bin/env bash
# Claude Code status line script
# Displays: model name | usage% (used / total)
INPUT=$(cat)
node -e "
var d = JSON.parse(process.argv[1]);

// Model name: prefer display_name, fall back to id
var m = '';
if (d.model && d.model.display_name) {
  m = d.model.display_name;
} else if (d.model && d.model.id) {
  m = d.model.id;
} else {
  m = '?';
}
// Clean up any ANSI/terminal artifacts
m = m.replace(/\\[1m\]/g, '').replace(/\\u001b\\[\\d+m/g, '');

// Context window data
var cw = d.context_window || {};
var pct = cw.used_percentage;
var usage = cw.current_usage;
var totalSize = cw.context_window_size || 0;

var parts = [m];

// Show percentage
if (pct !== null && pct !== undefined) {
  parts.push(pct + '%');
}

// Show specific numbers: (input+output / total)
if (usage && totalSize > 0) {
  var used = (usage.input_tokens || 0) + (usage.output_tokens || 0);
  parts.push('(' + used.toLocaleString() + ' / ' + totalSize.toLocaleString() + ')');
}

console.log(parts.join(' | '));
" "$INPUT"
