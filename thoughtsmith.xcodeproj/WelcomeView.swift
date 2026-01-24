//
//  WelcomeView.swift
//  Thought Smith
//
//  Created by TheWinterShadow
//  Copyright © 2024 Thought Smith. All rights reserved.
//

import SwiftUI

struct WelcomeView: View {
    @Binding var isPresented: Bool
    @State private var currentPage = 0
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [Color.blue.opacity(0.3), Color.purple.opacity(0.3)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            VStack {
                // Page indicator
                HStack(spacing: 8) {
                    ForEach(0..<3) { index in
                        Circle()
                            .fill(currentPage == index ? Color.primary : Color.secondary.opacity(0.3))
                            .frame(width: 8, height: 8)
                    }
                }
                .padding(.top)
                
                // Content
                TabView(selection: $currentPage) {
                    WelcomePage1()
                        .tag(0)
                    WelcomePage2()
                        .tag(1)
                    WelcomePage3(isPresented: $isPresented)
                        .tag(2)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
            }
        }
    }
}

// MARK: - Page 1: Welcome
struct WelcomePage1: View {
    var body: some View {
        VStack(spacing: 30) {
            Spacer()
            
            Image(systemName: "sparkles")
                .font(.system(size: 80))
                .foregroundColor(.blue)
            
            Text("Welcome to\nThought Smith")
                .font(.largeTitle)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)
            
            Text("Your personal AI-powered journaling companion")
                .font(.title3)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            Spacer()
            
            VStack(alignment: .leading, spacing: 20) {
                FeatureRow(
                    icon: "message.fill",
                    title: "Natural Conversations",
                    description: "Journal through dialogue, using text or voice"
                )
                
                FeatureRow(
                    icon: "brain.head.profile",
                    title: "AI-Powered Insights",
                    description: "Get thoughtful questions and reflections"
                )
                
                FeatureRow(
                    icon: "book.pages.fill",
                    title: "Beautiful Entries",
                    description: "Transform conversations into formatted journals"
                )
            }
            .padding(.horizontal, 40)
            
            Spacer()
            
            Text("Swipe to continue →")
                .font(.caption)
                .foregroundColor(.secondary)
                .padding(.bottom, 30)
        }
    }
}

// MARK: - Page 2: Setup Required
struct WelcomePage2: View {
    var body: some View {
        VStack(spacing: 30) {
            Spacer()
            
            Image(systemName: "key.fill")
                .font(.system(size: 80))
                .foregroundColor(.green)
            
            Text("Quick Setup")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Just a few steps to get started")
                .font(.title3)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            Spacer()
            
            VStack(alignment: .leading, spacing: 24) {
                SetupStep(
                    number: 1,
                    icon: "key.fill",
                    title: "Get an API Key",
                    description: "Sign up with OpenAI, Anthropic, or Google Gemini"
                )
                
                SetupStep(
                    number: 2,
                    icon: "gearshape.fill",
                    title: "Configure Settings",
                    description: "Add your API key and choose a model"
                )
                
                SetupStep(
                    number: 3,
                    icon: "text.bubble.fill",
                    title: "Customize (Optional)",
                    description: "Personalize AI behavior and journal format"
                )
                
                SetupStep(
                    number: 4,
                    icon: "checkmark.circle.fill",
                    title: "Start Journaling!",
                    description: "Begin your first conversation"
                )
            }
            .padding(.horizontal, 40)
            
            Spacer()
            
            Text("Swipe to continue →")
                .font(.caption)
                .foregroundColor(.secondary)
                .padding(.bottom, 30)
        }
    }
}

// MARK: - Page 3: Get Started
struct WelcomePage3: View {
    @Binding var isPresented: Bool
    @EnvironmentObject private var navigationState: NavigationState
    
    var body: some View {
        VStack(spacing: 30) {
            Spacer()
            
            Image(systemName: "arrow.right.circle.fill")
                .font(.system(size: 80))
                .foregroundColor(.purple)
            
            Text("Ready to Begin?")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Let's set up your AI companion")
                .font(.title3)
                .foregroundColor(.secondary)
            
            Spacer()
            
            VStack(spacing: 16) {
                Button {
                    isPresented = false
                    navigationState.navigate(to: .settings)
                } label: {
                    HStack {
                        Image(systemName: "gearshape.fill")
                        Text("Go to Settings")
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(12)
                }
                
                Button {
                    isPresented = false
                } label: {
                    HStack {
                        Image(systemName: "play.fill")
                        Text("Start Without Setup")
                    }
                    .font(.headline)
                    .foregroundColor(.blue)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(12)
                }
            }
            .padding(.horizontal, 40)
            
            Spacer()
            
            VStack(spacing: 8) {
                HStack(spacing: 4) {
                    Image(systemName: "questionmark.circle.fill")
                        .foregroundColor(.blue)
                    Text("Tap the help button (❓) anytime for detailed guides")
                }
                .font(.caption)
                .foregroundColor(.secondary)
                
                Text("You can change settings anytime")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.bottom, 30)
        }
    }
}

// MARK: - Helper Components
struct FeatureRow: View {
    let icon: String
    let title: String
    let description: String
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.blue)
                .frame(width: 40, height: 40)
                .background(Color.blue.opacity(0.1))
                .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
    }
}

struct SetupStep: View {
    let number: Int
    let icon: String
    let title: String
    let description: String
    
    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color.green.opacity(0.2))
                    .frame(width: 50, height: 50)
                
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(.green)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
    }
}
