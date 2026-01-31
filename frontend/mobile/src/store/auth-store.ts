import { create } from 'zustand';
import * as SecureStore from 'expo-secure-store';

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: User, accessToken: string, refreshToken: string) => Promise<void>;
  logout: () => Promise<void>;
  updateTokens: (accessToken: string, refreshToken: string) => Promise<void>;
  loadStoredAuth: () => Promise<void>;
}

const AUTH_STORAGE_KEY = 'auth_data';

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,

  login: async (user, accessToken, refreshToken) => {
    const authData = { user, accessToken, refreshToken };
    await SecureStore.setItemAsync(AUTH_STORAGE_KEY, JSON.stringify(authData));
    set({
      user,
      accessToken,
      refreshToken,
      isAuthenticated: true,
    });
  },

  logout: async () => {
    await SecureStore.deleteItemAsync(AUTH_STORAGE_KEY);
    set({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
    });
  },

  updateTokens: async (accessToken, refreshToken) => {
    const currentState = get();
    const authData = {
      user: currentState.user,
      accessToken,
      refreshToken,
    };
    await SecureStore.setItemAsync(AUTH_STORAGE_KEY, JSON.stringify(authData));
    set({ accessToken, refreshToken });
  },

  loadStoredAuth: async () => {
    try {
      const storedData = await SecureStore.getItemAsync(AUTH_STORAGE_KEY);
      if (storedData) {
        const { user, accessToken, refreshToken } = JSON.parse(storedData);
        set({
          user,
          accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
        });
      } else {
        set({ isLoading: false });
      }
    } catch (error) {
      console.error('Failed to load auth data:', error);
      set({ isLoading: false });
    }
  },
}));
