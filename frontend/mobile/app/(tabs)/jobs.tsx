import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator, RefreshControl } from 'react-native';
import { pdfApi } from '../../src/lib/api-client';
import Toast from 'react-native-toast-message';

interface Job {
  id: string;
  type: string;
  status: string;
  createdAt: string;
}

export default function JobsScreen() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadJobs = async () => {
    try {
      const response = await pdfApi.getMyJobs(0, 20);
      setJobs(response.data.content);
    } catch (error) {
      Toast.show({
        type: 'error',
        text1: 'Error',
        text2: 'Failed to load jobs',
      });
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadJobs();
  }, []);

  const handleRefresh = () => {
    setIsRefreshing(true);
    loadJobs();
  };

  const getStatusStyle = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return { backgroundColor: '#dcfce7', color: '#16a34a' };
      case 'FAILED':
        return { backgroundColor: '#fee2e2', color: '#dc2626' };
      case 'PROCESSING':
        return { backgroundColor: '#fef3c7', color: '#d97706' };
      default:
        return { backgroundColor: '#e5e7eb', color: '#6b7280' };
    }
  };

  const getJobTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      MERGE: '🔗 Merge',
      SPLIT: '✂️ Split',
      COMPRESS: '📦 Compress',
      WATERMARK: '💧 Watermark',
      PROTECT: '🔒 Protect',
      UNLOCK: '🔓 Unlock',
    };
    return labels[type] || type;
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const renderJob = ({ item }: { item: Job }) => {
    const statusStyle = getStatusStyle(item.status);
    
    return (
      <View style={styles.jobCard}>
        <View style={styles.jobHeader}>
          <Text style={styles.jobType}>{getJobTypeLabel(item.type)}</Text>
          <View style={[styles.statusBadge, { backgroundColor: statusStyle.backgroundColor }]}>
            <Text style={[styles.statusText, { color: statusStyle.color }]}>
              {item.status}
            </Text>
          </View>
        </View>
        <Text style={styles.jobDate}>{formatDate(item.createdAt)}</Text>
      </View>
    );
  };

  if (isLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#2563eb" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {jobs.length === 0 ? (
        <View style={styles.emptyState}>
          <Text style={styles.emptyEmoji}>📋</Text>
          <Text style={styles.emptyTitle}>No jobs yet</Text>
          <Text style={styles.emptyText}>
            Your processing jobs will appear here
          </Text>
        </View>
      ) : (
        <FlatList
          data={jobs}
          keyExtractor={(item) => item.id}
          renderItem={renderJob}
          contentContainerStyle={styles.list}
          refreshControl={
            <RefreshControl
              refreshing={isRefreshing}
              onRefresh={handleRefresh}
              colors={['#2563eb']}
            />
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  list: {
    padding: 16,
    gap: 12,
  },
  jobCard: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  jobHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  jobType: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
  },
  jobDate: {
    fontSize: 13,
    color: '#6b7280',
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  emptyEmoji: {
    fontSize: 64,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 8,
  },
  emptyText: {
    fontSize: 14,
    color: '#6b7280',
    textAlign: 'center',
  },
});
