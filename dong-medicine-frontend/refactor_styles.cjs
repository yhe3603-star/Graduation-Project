const fs = require('fs');
const path = require('path');

const cssVariables = {
  '#1A5276': 'var(--dong-indigo)',
  '#0d3d5c': 'var(--dong-indigo-dark)',
  '#2e7d9a': 'var(--dong-indigo-light)',
  '#28B463': 'var(--dong-jade)',
  '#1e8e4a': 'var(--dong-jade-dark)',
  '#58d68d': 'var(--dong-jade-light)',
  '#c9a227': 'var(--dong-gold)',
  '#f5a623': 'var(--dong-gold-light)',
  '#b87333': 'var(--dong-copper)',
  '#f8f5f0': 'var(--bg-rice)',
  '#f0ebe3': 'var(--bg-rice-dark)',
  '#faf8f5': 'var(--bg-paper)',
  '#fffdf9': 'var(--bg-cream)',
  '#1a1a1a': 'var(--text-primary)',
  '#555555': 'var(--text-secondary)',
  '#555': 'var(--text-secondary)',
  '#888888': 'var(--text-muted)',
  '#888': 'var(--text-muted)',
  '#aaaaaa': 'var(--text-light)',
  '#aaa': 'var(--text-light)',
  '#ffffff': 'var(--text-inverse)',
  '#fff': 'var(--text-inverse)',
  '#eeeeee': 'var(--border-light)',
  '#eee': 'var(--border-light)',
  '#dddddd': 'var(--border-medium)',
  '#ddd': 'var(--border-medium)',
  '#cccccc': 'var(--border-dark)',
  '#ccc': 'var(--border-dark)',
};

const hexToVar = (hex) => {
  return cssVariables[hex.toLowerCase()] || hex;
};

function processFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf8');
  let changed = false;

  // Replace colors in content
  // Regex to match hex colors, excluding those inside URLs (like svg data URIs) if possible, but let's just match #hex
  // We need to be careful with SVGs in CSS, but let's try.
  const regex = /#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\b/g;
  
  const newContent = content.replace(regex, (match) => {
    // skip if inside a url() or specific cases?
    const lowerMatch = match.toLowerCase();
    if (cssVariables[lowerMatch]) {
      changed = true;
      return cssVariables[lowerMatch];
    }
    return match;
  });

  if (changed) {
    fs.writeFileSync(filePath, newContent, 'utf8');
    console.log(`Updated ${filePath}`);
  }
}

function traverseDir(dir) {
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      traverseDir(fullPath);
    } else if (fullPath.endsWith('.vue') || fullPath.endsWith('.css') || fullPath.endsWith('.js')) {
      if (!fullPath.includes('variables.css')) {
         processFile(fullPath);
      }
    }
  }
}

traverseDir(path.join(__dirname, 'src'));
