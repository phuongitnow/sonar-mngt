#!/bin/bash

echo "=================================="
echo "Push to GitHub Repository"
echo "=================================="
echo ""

# Check if remote is configured
if git remote get-url origin > /dev/null 2>&1; then
    echo "✅ Remote origin: $(git remote get-url origin)"
else
    echo "❌ Remote origin not configured"
    exit 1
fi

echo ""
echo "📝 Current status:"
git status -s

echo ""
read -p "Ready to push? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "🚀 Pushing to GitHub..."
    echo ""
    
    git push -u origin main
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Push successful!"
        echo ""
        echo "📍 View your repository at:"
        echo "   https://github.com/phuongitnow/sonarqube-admin-app"
    else
        echo ""
        echo "❌ Push failed!"
        echo ""
        echo "Common issues:"
        echo "  - Repository doesn't exist on GitHub"
        echo "  - Authentication failed (need token)"
        echo "  - No internet connection"
    fi
else
    echo ""
    echo "❌ Push cancelled"
fi

