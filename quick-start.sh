#!/bin/bash

echo "=================================="
echo "SonarQube Admin Application Setup"
echo "=================================="
echo ""

# Check Docker
echo "📦 Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker is installed"
echo ""

# Check .env file
echo "⚙️  Checking configuration..."
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    if [ -f env.example ]; then
        cp env.example .env
        echo "✅ Created .env file"
        echo ""
        echo "⚠️  Please edit .env file with your configuration:"
        echo "   - SONARQUBE_URL"
        echo "   - SONARQUBE_TOKEN"
        echo "   - Mail settings (optional)"
        echo ""
        read -p "Press Enter to open .env file for editing (or Ctrl+C to exit)..."
        
        # Try to open with default editor
        if command -v nano &> /dev/null; then
            nano .env
        elif command -v vi &> /dev/null; then
            vi .env
        elif command -v vim &> /dev/null; then
            vim .env
        fi
    else
        echo "❌ env.example file not found"
        exit 1
    fi
else
    echo "✅ .env file already exists"
fi

echo ""
echo "🚀 Starting services..."
echo ""

# Start services
docker-compose up -d

# Wait a bit for services to start
echo ""
echo "⏳ Waiting for services to start..."
sleep 5

# Check if services are running
echo ""
echo "🔍 Checking services status..."
docker-compose ps

echo ""
echo "✅ Setup complete!"
echo ""
echo "📍 Access points:"
echo "   Frontend:    http://localhost:4200"
echo "   API:         http://localhost:6996"
echo "   Swagger UI:  http://localhost:6996/swagger-ui.html"
echo "   Health:      http://localhost:6996/actuator/health"
echo ""
echo "📊 View logs:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Stop services:"
echo "   docker-compose down"
echo ""

