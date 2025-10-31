# Punchout UI Frontend (Next.js)

Next.js frontend application for the cXML Punchout Service.

## Tech Stack

- **Next.js 14** - React framework with App Router
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Axios** - HTTP client for API calls

## Development

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

The application will start on **http://localhost:3000**

### Build for Production

```bash
npm run build
npm start
```

## Project Structure

```
src/
├── app/                    # Next.js App Router pages
│   ├── layout.tsx         # Root layout with navigation
│   ├── page.tsx           # Home page
│   └── converter/
│       └── page.tsx       # Converter page
├── components/            # React components
│   └── ConverterForm.tsx  # cXML converter form
├── styles/               # Global styles
│   └── globals.css       # Tailwind CSS imports
└── lib/                  # Utilities (future)
```

## Environment Variables

Create a `.env.local` file:

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Features

- ✅ Modern React with Next.js 14
- ✅ TypeScript for type safety
- ✅ Tailwind CSS for responsive design
- ✅ API integration with backend on port 8080
- ✅ Sample cXML data loader
- ✅ Error handling and loading states

## Maven Integration

The frontend is integrated with Maven using `frontend-maven-plugin`:
- Automatically installs Node.js and npm during Maven build
- Runs `npm install` and `npm run build` as part of Maven lifecycle
- No need to install Node.js separately for CI/CD

```bash
# Build with Maven
mvn clean install
```
