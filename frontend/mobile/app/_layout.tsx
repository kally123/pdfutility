import { useEffect } from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import Toast from 'react-native-toast-message';
import { useAuthStore } from '../src/store/auth-store';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000,
      retry: 1,
    },
  },
});

export default function RootLayout() {
  const loadStoredAuth = useAuthStore((state) => state.loadStoredAuth);

  useEffect(() => {
    loadStoredAuth();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <SafeAreaProvider>
        <StatusBar style="auto" />
        <Stack
          screenOptions={{
            headerStyle: {
              backgroundColor: '#2563eb',
            },
            headerTintColor: '#fff',
            headerTitleStyle: {
              fontWeight: 'bold',
            },
          }}
        >
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen name="login" options={{ title: 'Sign In' }} />
          <Stack.Screen name="register" options={{ title: 'Create Account' }} />
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        </Stack>
        <Toast />
      </SafeAreaProvider>
    </QueryClientProvider>
  );
}
