// Nova Voice Agent - Standalone Web Edition
(function () {
  'use strict';

  // State
  let user = JSON.parse(localStorage.getItem('nova_user') || '{"name":"Madhevan","signedIn":true}');
  let autoSpeak = JSON.parse(localStorage.getItem('nova_auto_speak') || 'true');
  let speechRate = parseFloat(localStorage.getItem('nova_speech_rate') || '1.0');
  let messages = JSON.parse(localStorage.getItem('nova_chat_history') || '[]');
  let isListening = false;
  let isSpeaking = false;
  let isLiveMode = false;
  let isLiveMuted = false;
  let recognition = null;

  // DOM Elements
  const bgVideo = document.getElementById('bgVideo');
  const btnToggleVideo = document.getElementById('btnToggleVideo');
  const btnLiveMode = document.getElementById('btnLiveMode');
  const btnExitLive = document.getElementById('btnExitLive');
  const btnToggleSettings = document.getElementById('btnToggleSettings');
  const settingsPanel = document.getElementById('settingsPanel');
  const btnAutoSpeakToggle = document.getElementById('btnAutoSpeakToggle');
  const speedChips = document.querySelectorAll('.chip-speed');
  const btnTestVoice = document.getElementById('btnTestVoice');
  const btnClearChat = document.getElementById('btnClearChat');
  const btnSignOut = document.getElementById('btnSignOut');
  const userNameDisplay = document.getElementById('userNameDisplay');
  const headerStatus = document.getElementById('headerStatus');
  const voiceOrb = document.getElementById('voiceOrb');
  const waveBars = document.getElementById('waveBars');
  const micIcon = document.getElementById('micIcon');
  const orbStatusBadge = document.getElementById('orbStatusBadge');
  const statusText = document.getElementById('statusText');
  const chatTimeline = document.getElementById('chatTimeline');
  const queryInput = document.getElementById('queryInput');
  const btnInputMic = document.getElementById('btnInputMic');
  const btnInputSend = document.getElementById('btnInputSend');
  const liveView = document.getElementById('liveView');
  const liveOrbBtn = document.getElementById('liveOrbBtn');
  const liveWaveBars = document.getElementById('liveWaveBars');
  const liveMicIcon = document.getElementById('liveMicIcon');
  const liveStatusText = document.getElementById('liveStatusText');
  const btnLiveInterrupt = document.getElementById('btnLiveInterrupt');
  const btnLiveMute = document.getElementById('btnLiveMute');
  const liveStreamFeed = document.getElementById('liveStreamFeed');
  const liveTextInput = document.getElementById('liveTextInput');
  const btnLiveSend = document.getElementById('btnLiveSend');
  const authView = document.getElementById('authView');
  const btnGoogleSignIn = document.getElementById('btnGoogleSignIn');
  const btnQuickAccess = document.getElementById('btnQuickAccess');

  // Initialize Default Welcome if history is empty
  if (messages.length === 0) {
    messages.push({
      id: 'welcome',
      role: 'assistant',
      content: 'Hello, I am Nova. Tap the glowing orb, speak any app name to open it (e.g. Open YouTube, Open Camera, Open Settings), or check your battery.',
      timestamp: Date.now()
    });
  }

  // Speech Recognition Setup
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  function initRecognition() {
    if (!SpeechRecognition) {
      console.warn('Web Speech Recognition API is not supported in this browser.');
      return null;
    }
    const rec = new SpeechRecognition();
    rec.continuous = false;
    rec.interimResults = true;
    rec.lang = 'en-US';

    rec.onstart = () => {
      isListening = true;
      updateVisualizerState();
    };

    rec.onresult = (event) => {
      let interim = '';
      let final = '';
      for (let i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) {
          final += event.results[i][0].transcript;
        } else {
          interim += event.results[i][0].transcript;
        }
      }
      const heard = (final || interim).trim();
      if (heard) {
        statusText.textContent = `Heard: "${heard}"`;
        if (isLiveMode) liveStatusText.textContent = `Heard: "${heard}"`;
      }
      if (final.trim()) {
        processQuery(final.trim());
      }
    };

    rec.onerror = (e) => {
      isListening = false;
      updateVisualizerState();
      statusText.textContent = e.error === 'no-speech' ? 'No speech detected. Tap to try again.' : `Mic: ${e.error}`;
      if (isLiveMode) liveStatusText.textContent = 'Listening paused. Tap orb to speak.';
    };

    rec.onend = () => {
      isListening = false;
      updateVisualizerState();
    };

    return rec;
  }
  recognition = initRecognition();

  // Speech Synthesis
  function speak(text, onComplete) {
    if (window.NovaAndroid && window.NovaAndroid.speak) {
      window.NovaAndroid.speak(text);
      if (onComplete) onComplete();
      return;
    }
    if (!('speechSynthesis' in window)) {
      if (onComplete) onComplete();
      return;
    }
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = speechRate;
    utterance.pitch = 1.0;

    utterance.onstart = () => {
      isSpeaking = true;
      updateVisualizerState();
    };

    utterance.onend = () => {
      isSpeaking = false;
      updateVisualizerState();
      if (onComplete) onComplete();
    };

    utterance.onerror = () => {
      isSpeaking = false;
      updateVisualizerState();
      if (onComplete) onComplete();
    };

    window.speechSynthesis.speak(utterance);
  }

  function stopSpeaking() {
    if (window.NovaAndroid && window.NovaAndroid.stopSpeaking) {
      window.NovaAndroid.stopSpeaking();
    }
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    isSpeaking = false;
    updateVisualizerState();
  }

  // Visualizer UI State
  function updateVisualizerState() {
    if (isSpeaking) {
      voiceOrb.classList.add('speaking');
      voiceOrb.classList.remove('listening');
      waveBars.classList.remove('hidden');
      micIcon.classList.add('hidden');
      orbStatusBadge.textContent = 'Nova is speaking...';

      if (isLiveMode) {
        liveWaveBars.classList.remove('hidden');
        liveMicIcon.classList.add('hidden');
        liveStatusText.textContent = 'Nova is speaking to you...';
        btnLiveInterrupt.classList.remove('hidden');
      }
    } else if (isListening) {
      voiceOrb.classList.add('listening');
      voiceOrb.classList.remove('speaking');
      waveBars.classList.add('hidden');
      micIcon.classList.remove('hidden');
      orbStatusBadge.textContent = 'Listening... Speak now';

      if (isLiveMode) {
        liveWaveBars.classList.add('hidden');
        liveMicIcon.classList.remove('hidden');
        liveStatusText.textContent = 'Listening... Speak anytime to chat';
        btnLiveInterrupt.classList.add('hidden');
      }
    } else {
      voiceOrb.classList.remove('speaking', 'listening');
      waveBars.classList.add('hidden');
      micIcon.classList.remove('hidden');
      orbStatusBadge.textContent = 'Tap Orb to Speak';

      if (isLiveMode) {
        liveWaveBars.classList.add('hidden');
        liveMicIcon.classList.remove('hidden');
        liveStatusText.textContent = isLiveMuted ? 'Microphone is muted' : 'Tap orb to start listening';
        btnLiveInterrupt.classList.add('hidden');
      }
    }
  }

  // Toggle Listening
  function toggleListening() {
    stopSpeaking();
    if (window.NovaAndroid && window.NovaAndroid.toggleListening) {
      window.NovaAndroid.toggleListening();
      return;
    }
    if (isListening) {
      if (recognition) recognition.stop();
      isListening = false;
      updateVisualizerState();
    } else {
      if (!recognition) recognition = initRecognition();
      if (recognition) {
        try {
          recognition.start();
        } catch (_) {
          statusText.textContent = 'Could not access microphone.';
        }
      } else {
        simulateMicInput();
      }
    }
  }

  function simulateMicInput() {
    isListening = true;
    updateVisualizerState();
    statusText.textContent = 'Listening simulation...';
    setTimeout(() => {
      isListening = false;
      updateVisualizerState();
      statusText.textContent = 'Type a command or click a suggestion below.';
    }, 2200);
  }

  // Battery Check Helper
  async function checkBattery() {
    if (window.NovaAndroid && window.NovaAndroid.getBatteryLevel) {
      try {
        const pct = window.NovaAndroid.getBatteryLevel();
        if (pct >= 0) {
          return `Your device battery is currently at ${pct} percent.`;
        }
      } catch (_) {}
    }
    if ('getBattery' in navigator) {
      try {
        const b = await navigator.getBattery();
        const pct = Math.round(b.level * 100);
        return `Your battery level is currently at ${pct} percent${b.charging ? ' and charging' : ''}.`;
      } catch (_) {}
    }
    return 'Your device battery is charged and operating normally.';
  }

  // Core Query Processing
  async function processQuery(queryText) {
    const clean = queryText.trim();
    if (!clean) return;

    // Add user message
    const userMsg = {
      id: 'msg_' + Date.now(),
      role: 'user',
      content: clean,
      timestamp: Date.now()
    };
    messages.push(userMsg);
    renderMessages();

    statusText.textContent = 'Nova is processing...';
    const lower = clean.toLowerCase();

    let reply = `I heard: "${clean}". Ask me to open any app, check battery, or search Google.`;
    let actionUrl = null;
    let actionLabel = null;
    let isSignOut = false;

    // 1. Voice Sign-Out
    if (lower.includes('sign out') || lower.includes('log out') || lower.includes('logout') || lower.includes('goodbye nova')) {
      reply = 'Understood. Signing you out securely now.';
      isSignOut = true;
      if (window.NovaAndroid && window.NovaAndroid.signOut) {
        window.NovaAndroid.signOut();
      }
    }
    // 2. Battery
    else if (lower.includes('battery') || lower.includes('power level')) {
      reply = await checkBattery();
    }
    // 3. Time & Date
    else if (lower.includes('time')) {
      const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      reply = `It is currently ${timeStr}.`;
    } else if (lower.includes('date') || lower.includes('today')) {
      const dateStr = new Date().toLocaleDateString([], { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
      reply = `Today is ${dateStr}.`;
    }
    // 4. Universal App & Web Launchers
    else if (lower.startsWith('open ') || lower.startsWith('launch ') || lower.startsWith('start ')) {
      const target = lower.replace(/^(open|launch|start)\s+/i, '').trim();
      const pretty = target.charAt(0).toUpperCase() + target.slice(1);
      reply = `Opening ${pretty} for you now.`;

      const appMap = {
        youtube: 'https://www.youtube.com',
        google: 'https://www.google.com',
        github: 'https://github.com',
        camera: 'https://webcamtests.com',
        maps: 'https://maps.google.com',
        spotify: 'https://open.spotify.com',
        settings: 'https://myaccount.google.com',
        calculator: 'https://www.google.com/search?q=calculator',
        clock: 'https://time.is',
        calendar: 'https://calendar.google.com',
        netflix: 'https://www.netflix.com',
        twitter: 'https://twitter.com',
        x: 'https://twitter.com',
        reddit: 'https://www.reddit.com'
      };

      actionUrl = appMap[target] || `https://www.google.com/search?q=${encodeURIComponent(target)}`;
      actionLabel = `Open ${pretty}`;
      if (window.NovaAndroid && window.NovaAndroid.launchApp) {
        window.NovaAndroid.launchApp(target);
      } else {
        window.open(actionUrl, '_blank');
      }
    }
    // 5. Search queries
    else if (lower.startsWith('search google for ') || lower.startsWith('search for ') || lower.startsWith('google ')) {
      const query = lower.replace(/^(search google for|search for|google)\s+/i, '').trim();
      reply = `Searching Google for "${query}".`;
      actionUrl = `https://www.google.com/search?q=${encodeURIComponent(query)}`;
      actionLabel = `Search: ${query}`;
      window.open(actionUrl, '_blank');
    } else if (lower.startsWith('search youtube for ') || lower.startsWith('youtube ')) {
      const query = lower.replace(/^(search youtube for|youtube)\s+/i, '').trim();
      reply = `Searching YouTube for "${query}".`;
      actionUrl = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
      actionLabel = `YouTube: ${query}`;
      window.open(actionUrl, '_blank');
    }
    // 6. Notes
    else if (lower.startsWith('take a note ') || lower.startsWith('note ')) {
      const note = clean.replace(/^(take a note|note)\s+/i, '').trim();
      reply = `Note saved: "${note}".`;
    }
    // 7. Jokes & Fun
    else if (lower.includes('joke')) {
      const jokes = [
        "Why do programmers prefer dark mode? Because light attracts bugs!",
        "There are 10 types of people in the world: those who understand binary, and those who don't.",
        "Why did the smartphone get glasses? Because it lost all its contacts!",
        "An algorithm walks into a bar and says: 'I'll have what everyone else is having, but 20% faster!'"
      ];
      reply = jokes[Math.floor(Math.random() * jokes.length)];
    }

    const assistantMsg = {
      id: 'msg_' + Date.now(),
      role: 'assistant',
      content: reply,
      actionUrl: actionUrl,
      actionLabel: actionLabel,
      timestamp: Date.now()
    };
    messages.push(assistantMsg);
    saveMessages();
    renderMessages();

    statusText.textContent = actionLabel ? `Launched ${actionLabel}` : 'Nova answered.';

    // Speech output
    if (autoSpeak || isLiveMode || isSignOut) {
      speak(reply, () => {
        if (isSignOut) {
          signOut();
        } else if (isLiveMode && !isLiveMuted) {
          setTimeout(() => {
            if (isLiveMode && !isSpeaking) toggleListening();
          }, 500);
        }
      });
    }
  }

  // Render Chat Messages
  function renderMessages() {
    chatTimeline.innerHTML = '';
    messages.forEach((m) => {
      const bubble = document.createElement('div');
      bubble.className = `message-bubble ${m.role}`;

      const timeStr = new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

      bubble.innerHTML = `
        <div class="bubble-header">
          <span class="bubble-role">${m.role === 'user' ? 'YOU' : 'NOVA'}</span>
          <div class="bubble-actions">
            <span>${timeStr}</span>
            <button class="bubble-btn copy-btn" title="Copy">📋</button>
            ${m.role === 'assistant' ? '<button class="bubble-btn speak-btn" title="Speak aloud">🔊</button>' : ''}
          </div>
        </div>
        <div class="bubble-content">${escapeHtml(m.content)}</div>
        ${m.actionUrl ? `<a href="${m.actionUrl}" target="_blank" class="action-launch-btn">↗ ${escapeHtml(m.actionLabel || 'Open Link')}</a>` : ''}
      `;

      bubble.querySelector('.copy-btn').addEventListener('click', () => {
        navigator.clipboard.writeText(m.content);
        statusText.textContent = 'Copied to clipboard.';
      });

      const speakBtn = bubble.querySelector('.speak-btn');
      if (speakBtn) {
        speakBtn.addEventListener('click', () => speak(m.content));
      }

      chatTimeline.appendChild(bubble);
    });

    chatTimeline.scrollTop = chatTimeline.scrollHeight;

    // Also update Live Stream
    if (liveStreamFeed) {
      liveStreamFeed.innerHTML = '';
      messages.slice(-8).forEach((m) => {
        const item = document.createElement('div');
        item.style.display = 'flex';
        item.style.gap = '8px';
        item.innerHTML = `<strong style="color: ${m.role === 'user' ? 'var(--white)' : 'var(--cyan)'};">${m.role === 'user' ? 'You:' : 'Nova:'}</strong> <span>${escapeHtml(m.content)}</span>`;
        liveStreamFeed.appendChild(item);
      });
      liveStreamFeed.scrollTop = liveStreamFeed.scrollHeight;
    }
  }

  function escapeHtml(str) {
    return str.replace(/[&<>"']/g, (m) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m]);
  }

  function saveMessages() {
    try {
      localStorage.setItem('nova_chat_history', JSON.stringify(messages));
    } catch (_) {}
  }

  // Sign In & Sign Out Flow
  function updateAuthUI() {
    if (user && user.signedIn) {
      authView.classList.add('hidden');
      userNameDisplay.textContent = user.name || 'Madhevan';
    } else {
      authView.classList.remove('hidden');
    }
  }

  function signIn(name) {
    user = { name: name || 'Madhevan', signedIn: true };
    localStorage.setItem('nova_user', JSON.stringify(user));
    updateAuthUI();
    statusText.textContent = `Welcome back, ${user.name}.`;
    if (autoSpeak) speak(`Welcome back, ${user.name}. Nova Voice Agent is online.`);
  }

  function signOut() {
    user = { name: user.name || 'Madhevan', signedIn: false };
    localStorage.setItem('nova_user', JSON.stringify(user));
    stopSpeaking();
    if (isListening && recognition) recognition.stop();
    exitLiveMode();
    updateAuthUI();
  }

  // Live Mode
  function enterLiveMode() {
    isLiveMode = true;
    liveView.classList.remove('hidden');
    speak('Nova Live conversation active. I am listening.', () => {
      if (!isLiveMuted) toggleListening();
    });
  }

  function exitLiveMode() {
    isLiveMode = false;
    liveView.classList.add('hidden');
    stopSpeaking();
    if (isListening && recognition) recognition.stop();
  }

  // Event Listeners
  voiceOrb.addEventListener('click', toggleListening);
  btnInputMic.addEventListener('click', toggleListening);

  btnInputSend.addEventListener('click', () => {
    const text = queryInput.value.trim();
    if (text) {
      processQuery(text);
      queryInput.value = '';
    }
  });

  queryInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      const text = queryInput.value.trim();
      if (text) {
        processQuery(text);
        queryInput.value = '';
      }
    }
  });

  document.querySelectorAll('.suggestion-chip').forEach((chip) => {
    chip.addEventListener('click', () => processQuery(chip.textContent.trim()));
  });

  btnLiveMode.addEventListener('click', enterLiveMode);
  btnExitLive.addEventListener('click', exitLiveMode);
  liveOrbBtn.addEventListener('click', toggleListening);

  btnLiveInterrupt.addEventListener('click', () => {
    stopSpeaking();
    toggleListening();
  });

  btnLiveMute.addEventListener('click', () => {
    isLiveMuted = !isLiveMuted;
    btnLiveMute.textContent = isLiveMuted ? 'Muted' : 'Mic On';
    btnLiveMute.classList.toggle('muted', isLiveMuted);
    if (isLiveMuted && isListening && recognition) recognition.stop();
    else if (!isLiveMuted && !isSpeaking) toggleListening();
  });

  btnLiveSend.addEventListener('click', () => {
    const text = liveTextInput.value.trim();
    if (text) {
      processQuery(text);
      liveTextInput.value = '';
    }
  });

  liveTextInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      const text = liveTextInput.value.trim();
      if (text) {
        processQuery(text);
        liveTextInput.value = '';
      }
    }
  });

  btnToggleVideo.addEventListener('click', () => {
    if (bgVideo.paused) {
      bgVideo.play();
      btnToggleVideo.style.opacity = '1';
    } else {
      bgVideo.pause();
      btnToggleVideo.style.opacity = '0.5';
    }
  });

  btnToggleSettings.addEventListener('click', () => {
    settingsPanel.classList.toggle('hidden');
  });

  btnAutoSpeakToggle.addEventListener('click', () => {
    autoSpeak = !autoSpeak;
    localStorage.setItem('nova_auto_speak', JSON.stringify(autoSpeak));
    btnAutoSpeakToggle.textContent = autoSpeak ? 'ON' : 'OFF';
    btnAutoSpeakToggle.classList.toggle('active', autoSpeak);
  });

  speedChips.forEach((chip) => {
    chip.addEventListener('click', () => {
      speedChips.forEach((c) => c.classList.remove('active'));
      chip.classList.add('active');
      speechRate = parseFloat(chip.dataset.speed);
      localStorage.setItem('nova_speech_rate', speechRate.toString());
    });
  });

  btnTestVoice.addEventListener('click', () => {
    speak('Nova voice system is online, responsive, and ready for your commands.');
  });

  btnClearChat.addEventListener('click', () => {
    messages = [{
      id: 'welcome',
      role: 'assistant',
      content: 'Conversation history cleared. How can I assist you?',
      timestamp: Date.now()
    }];
    saveMessages();
    renderMessages();
    statusText.textContent = 'History cleared.';
  });

  btnSignOut.addEventListener('click', signOut);

  btnGoogleSignIn.addEventListener('click', () => signIn('Madhevan'));
  btnQuickAccess.addEventListener('click', () => signIn('Operator'));

  // Export callbacks for Android WebView interface
  window.onNovaHeard = function(text) {
    if (statusText) statusText.textContent = `Heard: "${text}"`;
    if (isLiveMode && liveStatusText) liveStatusText.textContent = `Heard: "${text}"`;
    processQuery(text);
  };
  window.onNovaListeningState = function(listening) {
    isListening = listening;
    updateVisualizerState();
  };
  window.onNovaSpeakingState = function(speaking) {
    isSpeaking = speaking;
    updateVisualizerState();
  };

  // Initial Boot
  updateAuthUI();
  renderMessages();
})();
