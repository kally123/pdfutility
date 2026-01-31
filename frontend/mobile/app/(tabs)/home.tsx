import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { useAuthStore } from '../../src/store/auth-store';

export default function HomeScreen() {
  const router = useRouter();
  const { user } = useAuthStore();

  const quickActions = [
    { emoji: '🔗', title: 'Merge', route: '/tools/merge', color: '#3b82f6' },
    { emoji: '✂️', title: 'Split', route: '/tools/split', color: '#22c55e' },
    { emoji: '📦', title: 'Compress', route: '/tools/compress', color: '#a855f7' },
    { emoji: '🔒', title: 'Protect', route: '/tools/protect', color: '#ef4444' },
  ];

  return (
    <ScrollView style={styles.container}>
      {/* Welcome Section */}
      <View style={styles.welcomeCard}>
        <Text style={styles.welcomeText}>Welcome back,</Text>
        <Text style={styles.userName}>{user?.firstName || 'User'}!</Text>
        <Text style={styles.welcomeSubtext}>
          What would you like to do today?
        </Text>
      </View>

      {/* Quick Actions */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Quick Actions</Text>
        <View style={styles.actionsGrid}>
          {quickActions.map((action, index) => (
            <TouchableOpacity
              key={index}
              style={[styles.actionCard, { backgroundColor: action.color + '15' }]}
              onPress={() => router.push(action.route as any)}
            >
              <View style={[styles.actionIcon, { backgroundColor: action.color }]}>
                <Text style={styles.actionEmoji}>{action.emoji}</Text>
              </View>
              <Text style={styles.actionTitle}>{action.title}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Stats Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Your Stats</Text>
        <View style={styles.statsRow}>
          <View style={styles.statCard}>
            <Text style={styles.statNumber}>0</Text>
            <Text style={styles.statLabel}>Files Processed</Text>
          </View>
          <View style={styles.statCard}>
            <Text style={styles.statNumber}>0 MB</Text>
            <Text style={styles.statLabel}>Storage Used</Text>
          </View>
        </View>
      </View>

      {/* Tips Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Tips</Text>
        <View style={styles.tipCard}>
          <Text style={styles.tipEmoji}>💡</Text>
          <View style={styles.tipContent}>
            <Text style={styles.tipTitle}>Did you know?</Text>
            <Text style={styles.tipText}>
              You can merge up to 20 PDF files at once using our merge tool.
            </Text>
          </View>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  welcomeCard: {
    backgroundColor: '#2563eb',
    padding: 24,
    margin: 16,
    borderRadius: 16,
  },
  welcomeText: {
    color: '#bfdbfe',
    fontSize: 16,
  },
  userName: {
    color: '#fff',
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  welcomeSubtext: {
    color: '#bfdbfe',
    fontSize: 14,
  },
  section: {
    padding: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 12,
  },
  actionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  actionCard: {
    width: '48%',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  actionIcon: {
    width: 48,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  actionEmoji: {
    fontSize: 24,
  },
  actionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  statsRow: {
    flexDirection: 'row',
    gap: 12,
  },
  statCard: {
    flex: 1,
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  statNumber: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#2563eb',
  },
  statLabel: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 4,
  },
  tipCard: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'flex-start',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  tipEmoji: {
    fontSize: 28,
    marginRight: 12,
  },
  tipContent: {
    flex: 1,
  },
  tipTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 4,
  },
  tipText: {
    fontSize: 14,
    color: '#6b7280',
    lineHeight: 20,
  },
});
