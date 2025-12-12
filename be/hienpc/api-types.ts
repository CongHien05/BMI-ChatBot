/**
 * TypeScript Type Definitions for BMI Chatbot API
 * 
 * File này chứa tất cả types/interfaces cho Frontend
 * Copy file này vào project FE và import khi cần
 * 
 * Base URL: http://localhost:8080
 */

// ============================================================================
// REQUEST TYPES
// ============================================================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName?: string;
}

export interface ProfileUpdateRequest {
  dateOfBirth?: string; // Format: "YYYY-MM-DD"
  gender?: "MALE" | "FEMALE" | "OTHER";
  goalType?: "LOSE_WEIGHT" | "GAIN_WEIGHT" | "MAINTAIN_WEIGHT";
  goalWeightKg?: number; // Must be > 0
  dailyCalorieGoal?: number; // Must be >= 0
}

export interface FoodLogRequest {
  foodId: number;
  quantity: number; // Must be > 0.01
  mealType: "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK";
}

export interface ExerciseLogRequest {
  exerciseId: number;
  durationMinutes: number; // Must be >= 1
}

export interface ChatRequest {
  message: string;
}

// ============================================================================
// RESPONSE TYPES
// ============================================================================

export interface JwtResponse {
  token: string;
  type: string; // Always "Bearer"
  email: string;
  userId: number;
}

export interface DashboardSummary {
  currentWeight?: number | null;
  bmi?: number | null;
  totalCaloriesToday: number;
}

export interface FoodResponse {
  foodId: number;
  foodName: string;
  servingUnit: string;
  caloriesPerUnit: number;
}

export interface ExerciseResponse {
  exerciseId: number;
  exerciseName: string;
  caloriesBurnedPerHour: number;
}

export interface ChatResponse {
  reply: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// ============================================================================
// API CLIENT TYPES (Optional - for typed API client)
// ============================================================================

export interface ApiClientConfig {
  baseURL: string;
  getToken?: () => string | null;
  onTokenExpired?: () => void;
}

export class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public response?: ErrorResponse
  ) {
    super(message);
    this.name = "ApiError";
  }
}

// ============================================================================
// EXAMPLE USAGE
// ============================================================================

/*
// Example: Using with fetch
async function login(email: string, password: string): Promise<JwtResponse> {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password } as LoginRequest),
  });

  if (!response.ok) {
    const error: ErrorResponse = await response.json();
    throw new ApiError(response.status, error.message, error);
  }

  return response.json();
}

// Example: Using with axios
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiry
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired, redirect to login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Use typed API
async function getDashboardSummary(): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/api/dashboard/summary');
  return response.data;
}
*/
