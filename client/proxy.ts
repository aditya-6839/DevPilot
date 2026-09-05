import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

const AUTH_COOKIE = "devpilot_auth"

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl

  const isAuthed = request.cookies.get(AUTH_COOKIE)?.value === "1"

  // Allow OAuth callback through without the frontend cookie yet
  if (pathname.startsWith("/auth/callback")) {
    return NextResponse.next()
  }

  // Protect dashboard and chat routes
  if (pathname.startsWith("/dashboard") || pathname.startsWith("/chat")) {
    if (!isAuthed) {
      const loginUrl = request.nextUrl.clone()
      loginUrl.pathname = "/login"
      loginUrl.searchParams.set("next", pathname)

      return NextResponse.redirect(loginUrl)
    }
  }

  // Prevent authenticated users from going back to login
  if (pathname === "/login" && isAuthed) {
    const dashboardUrl = request.nextUrl.clone()
    dashboardUrl.pathname = "/dashboard"

    return NextResponse.redirect(dashboardUrl)
  }

  return NextResponse.next()
}

export const config = {
  matcher: [
    "/dashboard/:path*",
    "/chat/:path*",
    "/login",
    "/auth/callback",
  ],
}