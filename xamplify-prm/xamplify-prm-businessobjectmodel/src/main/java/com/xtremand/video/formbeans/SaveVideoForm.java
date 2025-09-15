package com.xtremand.video.formbeans;

import java.util.List;

public class SaveVideoForm {
		Integer id;
		String title;
		List<String> imageFiles;
		List<String> viewBy;
		String error;
		List<String> gifFiles;
		int videoLength;
		int bitRate;
		String videoPath;
		
		public SaveVideoForm(){
			
		}
		
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<String> getImageFiles() {
			return imageFiles;
		}

		public void setImageFiles(List<String> imageFiles) {
			this.imageFiles = imageFiles;
		}

		public List<String> getViewBy() {
			return viewBy;
		}

		public void setViewBy(List<String> viewBy) {
			this.viewBy = viewBy;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public List<String> getGifFiles() {
			return gifFiles;
		}

		public void setGifFiles(List<String> gifFiles) {
			this.gifFiles = gifFiles;
		}

		public int getVideoLength() {
			return videoLength;
		}

		public void setVideoLength(int videoLength) {
			this.videoLength = videoLength;
		}

		public int getBitRate() {
			return bitRate;
		}

		public void setBitRate(int bitRate) {
			this.bitRate = bitRate;
		}

		public String getVideoPath() {
			return videoPath;
		}

		public void setVideoPath(String videoPath) {
			this.videoPath = videoPath;
		}

		@Override
		public String toString() {
			return "SaveVideoForm [id=" + id + ", title=" + title + ", imageFiles=" + imageFiles + ", viewBy=" + viewBy
					+ ", error=" + error + ", gifFiles=" + gifFiles + ", videoLength=" + videoLength + ", bitRate="
					+ bitRate + ", videoPath=" + videoPath + "]";
		}
	}
