import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuthStore } from '../../src/store/auth-store';
import { authApi } from '../../src/lib/api-client';
import Toast from 'react-native-toast-message';

export default function ProfileScreen() {
  const router = useRouter();
  const { user, refreshToken, logout } = useAuthStore();

  const handleLogout = () => {
    Alert.alert(
      'Logout',
      'Are you sure you want to logout?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Logout',
          style: 'destructive',
          onPress: async () => {
            try {
              if (refreshToken) {
                await authApi.logout(refreshToken);
              }
            } catch (error) {
              // Ignore logout errors
            } finally {
              await logout();
              Toast.show({
                type: 'success',
                text1: 'Logged out',
                text2: 'See you next time!',
              });
              router.replace('/');
            }
          },
        },
      ]
    );
  };

  const menuItems = [
    { emoji: '📊', title: 'Storage Usage', onPress: () => {} },
    { emoji: '⚙️', title: 'Settings', onPress: () => {} },
    { emoji: '❓', title: 'Help & Support', onPress: () => {} },
    { emoji: '📄', title: 'Terms of Service', onPress: () => {} },
    { emoji: '🔒', title: 'Privacy Policy', onPress: () => {} },
  ];

  return (
    <ScrollView style={styles.container}>
      {/* Profile Card */}
      <View style={styles.profileCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </Text>
        </View>
        <Text style={styles.name}>
          {user?.firstName} {user?.lastName}
        </Text>
        <Text style={styles.email}>{user?.email}</Text>
      </View>

      {/* Menu Items */}
      <View style={styles.menu}>
        {menuItems.map((item, index) => (
          <TouchableOpacity
            key={index}
            style={styles.menuItem}
            onPress={item.onPress}
          >
            <Text style={styles.menuEmoji}>{item.emoji}</Text>
            <Text style={styles.menuTitle}>{item.title}</Text>
            <Text style={styles.menuArrow}>→</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Logout Button */}
      <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
        <Text style={styles.logoutText}>🚪 Logout</Text>
      </TouchableOpacity>

      {/* App Version */}
      <Text style={styles.version}>Version 1.0.0</Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  profileCard: {
    backgroundColor: '#fff',
    margin: 16,
    padding: 24,
    borderRadius: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#2563eb',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  avatarText: {
    color: '#fff',
    fontSize: 28,
    fontWeight: 'bold',
  },
  name: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  email: {
    fontSize: 14,
    color: '#6b7280',
  },
  menu: {
    backgroundColor: '#fff',
    marginHorizontal: 16,
    borderRadius: 16,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  menuEmoji: {
    fontSize: 20,
    marginRight: 12,
  },
  menuTitle: {
    flex: 1,
    fontSize: 16,
    color: '#374151',
  },
  menuArrow: {
    fontSize: 16,
    color: '#9ca3af',
  },
  logoutButton: {
    backgroundColor: '#fee2e2',
    margin: 16,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  logoutText: {
    color: '#dc2626',
    fontSize: 16,
    fontWeight: '600',
  },
  version: {
    textAlign: 'center',
    color: '#9ca3af',
    fontSize: 12,
    marginBottom: 32,
  },
});
