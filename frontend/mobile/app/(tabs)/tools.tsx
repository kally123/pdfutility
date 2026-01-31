import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function ToolsScreen() {
  const router = useRouter();

  const tools = [
    {
      emoji: '🔗',
      title: 'Merge PDFs',
      description: 'Combine multiple PDF files into one',
      route: '/tools/merge',
      color: '#3b82f6',
    },
    {
      emoji: '✂️',
      title: 'Split PDF',
      description: 'Extract specific pages from PDF',
      route: '/tools/split',
      color: '#22c55e',
    },
    {
      emoji: '📦',
      title: 'Compress PDF',
      description: 'Reduce file size while maintaining quality',
      route: '/tools/compress',
      color: '#a855f7',
    },
    {
      emoji: '💧',
      title: 'Add Watermark',
      description: 'Add text watermark to PDF pages',
      route: '/tools/watermark',
      color: '#f97316',
    },
    {
      emoji: '🔒',
      title: 'Protect PDF',
      description: 'Add password protection to PDF',
      route: '/tools/protect',
      color: '#ef4444',
    },
    {
      emoji: '🔓',
      title: 'Unlock PDF',
      description: 'Remove password from PDF',
      route: '/tools/unlock',
      color: '#14b8a6',
    },
  ];

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>PDF Tools</Text>
        <Text style={styles.headerSubtitle}>
          Select a tool to process your PDF files
        </Text>
      </View>

      <View style={styles.toolsList}>
        {tools.map((tool, index) => (
          <TouchableOpacity
            key={index}
            style={styles.toolCard}
            onPress={() => router.push(tool.route as any)}
          >
            <View style={[styles.toolIcon, { backgroundColor: tool.color }]}>
              <Text style={styles.toolEmoji}>{tool.emoji}</Text>
            </View>
            <View style={styles.toolContent}>
              <Text style={styles.toolTitle}>{tool.title}</Text>
              <Text style={styles.toolDescription}>{tool.description}</Text>
            </View>
            <Text style={styles.arrow}>→</Text>
          </TouchableOpacity>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  header: {
    padding: 16,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 14,
    color: '#6b7280',
  },
  toolsList: {
    padding: 16,
    gap: 12,
  },
  toolCard: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 16,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  toolIcon: {
    width: 48,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  toolEmoji: {
    fontSize: 24,
  },
  toolContent: {
    flex: 1,
    marginLeft: 12,
  },
  toolTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 2,
  },
  toolDescription: {
    fontSize: 13,
    color: '#6b7280',
  },
  arrow: {
    fontSize: 20,
    color: '#9ca3af',
  },
});
