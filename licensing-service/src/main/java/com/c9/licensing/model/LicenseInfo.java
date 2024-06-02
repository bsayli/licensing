package com.c9.licensing.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record LicenseInfo(String userId, String licenseTier, String licenseStatus, LocalDateTime expirationDate,
		List<String> instanceIds, int maxCount, int remainingUsageCount) {

	// Builder (Nested Static Class)
	public static class Builder {
		private String userId;
		private String licenseTier;
		private String licenseStatus = "Active"; // Default to Active
		private LocalDateTime expirationDate;
		private List<String> instanceIds = new ArrayList<>();
		private int maxCount;
		private int remainingUsageCount;

		public Builder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public Builder licenseTier(String licenseTier) {
			this.licenseTier = licenseTier;
			return this;
		}

		public Builder licenseStatus(String licenseStatus) {
			this.licenseStatus = licenseStatus;
			return this;
		}

		public Builder expirationDate(LocalDateTime expirationDate) {
			this.expirationDate = expirationDate;
			return this;
		}

		public Builder instanceIds(List<String> instanceIds) {
			this.instanceIds.addAll(instanceIds);
			return this;
		}

		public Builder maxCount(int maxCount) {
			this.maxCount = maxCount;
			return this;
		}

		public Builder remainingUsageCount(int remainingUsageCount) {
			this.remainingUsageCount = remainingUsageCount;
			return this;
		}

		public LicenseInfo build() {
			if (userId == null || licenseTier == null || expirationDate == null || maxCount == 0
					|| licenseStatus == null) {
				throw new IllegalArgumentException("Required parameters are not set.");
			}
			return new LicenseInfo(userId, licenseTier, licenseStatus, expirationDate, instanceIds, maxCount,
					remainingUsageCount);
		}
	}
}
