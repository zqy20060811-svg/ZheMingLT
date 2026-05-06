<template>
  <div class="manor-background" :class="{ 'night-mode': isDarkMode }">
    <!-- 天空背景 -->
    <div class="sky"></div>
    
    <!-- 太阳/月亮 -->
    <div class="celestial-body"></div>
    
    <!-- 云朵 -->
    <div class="clouds">
      <div class="cloud cloud-1"></div>
      <div class="cloud cloud-2"></div>
      <div class="cloud cloud-3"></div>
      <div class="cloud cloud-4"></div>
    </div>
    
    <!-- 远山 -->
    <div class="mountains">
      <div class="mountain mountain-1"></div>
      <div class="mountain mountain-2"></div>
      <div class="mountain mountain-3"></div>
    </div>
    
    <!-- 树木 -->
    <div class="trees">
      <div class="tree tree-1">
        <div class="trunk"></div>
        <div class="foliage"></div>
      </div>
      <div class="tree tree-2">
        <div class="trunk"></div>
        <div class="foliage"></div>
      </div>
      <div class="tree tree-3">
        <div class="trunk"></div>
        <div class="foliage"></div>
      </div>
      <div class="tree tree-4">
        <div class="trunk"></div>
        <div class="foliage"></div>
      </div>
    </div>
    
    <!-- 草地 -->
    <div class="grass">
      <div class="grass-blade" v-for="n in 20" :key="n" :style="{ left: `${n * 5}%` }"></div>
    </div>
    
    <!-- 花朵 -->
    <div class="flowers">
      <div class="flower" v-for="n in 8" :key="n" :class="`flower-${n}`">
        <div class="stem"></div>
        <div class="petals"></div>
      </div>
    </div>
    
    <!-- 栅栏 -->
    <div class="fence">
      <div class="fence-post" v-for="n in 12" :key="n"></div>
      <div class="fence-rail rail-top"></div>
      <div class="fence-rail rail-bottom"></div>
    </div>
    
    <!-- 小鸟 -->
    <div class="birds">
      <div class="bird bird-1"></div>
      <div class="bird bird-2"></div>
      <div class="bird bird-3"></div>
    </div>
    
    <!-- 星星（仅夜晚模式） -->
    <div class="stars" v-if="isDarkMode">
      <div class="star" v-for="n in 30" :key="n" :class="`star-${n}`"></div>
    </div>
    
    <!-- 萤火虫（仅夜晚模式） -->
    <div class="fireflies" v-if="isDarkMode">
      <div class="firefly" v-for="n in 15" :key="n" :class="`firefly-${n}`"></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isDarkMode: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.manor-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

/* 天空 */
.sky {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, 
    #E8F4F8 0%, 
    #D4E8F0 30%, 
    #C8E0D8 60%, 
    #E0F0E8 100%
  );
  transition: background 1s ease;
}

.night-mode .sky {
  background: linear-gradient(180deg, 
    #1a1a2e 0%, 
    #16213e 30%, 
    #0f3460 60%, 
    #1a4a5e 100%
  );
}

/* 太阳/月亮 */
.celestial-body {
  position: absolute;
  top: 60px;
  right: 80px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #FFF8E7, #F5E6C8);
  box-shadow: 0 0 40px rgba(245, 230, 200, 0.5);
  transition: all 1s ease;
}

.night-mode .celestial-body {
  background: radial-gradient(circle at 30% 30%, #F5F5F5, #E0E0E0);
  box-shadow: 0 0 30px rgba(255, 255, 255, 0.3);
}

/* 云朵 */
.clouds {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 100px;
  opacity: 0.9;
}

.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 100px;
}

.cloud-1 {
  width: 100px;
  height: 40px;
  top: 15%;
  left: 10%;
  animation: float 20s infinite ease-in-out;
}

.cloud-1::before {
  width: 50px;
  height: 50px;
  top: -25px;
  left: 10px;
}

.cloud-1::after {
  width: 60px;
  height: 40px;
  top: -15px;
  right: 10px;
}

.cloud-2 {
  width: 80px;
  height: 35px;
  top: 25%;
  left: 60%;
  animation: float 25s infinite ease-in-out;
  animation-delay: -5s;
}

.cloud-2::before {
  width: 40px;
  height: 40px;
  top: -20px;
  left: 15px;
}

.cloud-2::after {
  width: 50px;
  height: 35px;
  top: -10px;
  right: 15px;
}

.cloud-3 {
  width: 120px;
  height: 45px;
  top: 10%;
  right: 20%;
  animation: float 30s infinite ease-in-out;
  animation-delay: -10s;
}

.cloud-3::before {
  width: 60px;
  height: 60px;
  top: -30px;
  left: 20px;
}

.cloud-3::after {
  width: 70px;
  height: 45px;
  top: -20px;
  right: 20px;
}

.cloud-4 {
  width: 90px;
  height: 38px;
  top: 35%;
  right: 5%;
  animation: float 22s infinite ease-in-out;
  animation-delay: -15s;
}

.night-mode .cloud {
  background: rgba(200, 200, 220, 0.3);
  opacity: 0.5;
}

.night-mode .cloud::before,
.night-mode .cloud::after {
  background: rgba(200, 200, 220, 0.3);
}

@keyframes float {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(30px); }
}

/* 远山 */
.mountains {
  position: absolute;
  bottom: 25%;
  left: 0;
  right: 0;
  height: 200px;
}

.mountain {
  position: absolute;
  bottom: 0;
  width: 0;
  height: 0;
  border-style: solid;
}

.mountain-1 {
  left: -5%;
  border-left-width: 300px;
  border-right-width: 300px;
  border-bottom-width: 180px;
  border-left-color: transparent;
  border-right-color: transparent;
  border-bottom-color: #A8C5B5;
}

.mountain-2 {
  left: 25%;
  border-left-width: 250px;
  border-right-width: 250px;
  border-bottom-width: 150px;
  border-left-color: transparent;
  border-right-color: transparent;
  border-bottom-color: #B8D4C5;
}

.mountain-3 {
  right: -10%;
  border-left-width: 350px;
  border-right-width: 350px;
  border-bottom-width: 200px;
  border-left-color: transparent;
  border-right-color: transparent;
  border-bottom-color: #9CB8A8;
}

.night-mode .mountain-1 {
  border-bottom-color: #2a3a4a;
}

.night-mode .mountain-2 {
  border-bottom-color: #3a4a5a;
}

.night-mode .mountain-3 {
  border-bottom-color: #1a2a3a;
}

/* 树木 */
.trees {
  position: absolute;
  bottom: 15%;
  left: 0;
  right: 0;
  height: 150px;
}

.tree {
  position: absolute;
  bottom: 0;
}

.trunk {
  width: 12px;
  height: 40px;
  background: linear-gradient(90deg, #8B7355, #A0826D);
  border-radius: 2px;
  margin: 0 auto;
}

.foliage {
  width: 60px;
  height: 80px;
  background: radial-gradient(ellipse at center, #7CB87C, #5A9A5A);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  margin-bottom: -10px;
}

.tree-1 { left: 5%; transform: scale(0.8); }
.tree-2 { left: 15%; transform: scale(1.2); }
.tree-3 { right: 10%; transform: scale(0.9); }
.tree-4 { right: 25%; transform: scale(1.1); }

.night-mode .trunk {
  background: linear-gradient(90deg, #4a3a2a, #5a4a3a);
}

.night-mode .foliage {
  background: radial-gradient(ellipse at center, #2a4a3a, #1a3a2a);
}

/* 草地 */
.grass {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 18%;
  background: linear-gradient(180deg, #8FBC8F, #7CB87C);
}

.night-mode .grass {
  background: linear-gradient(180deg, #2a4a3a, #1a3a2a);
}

.grass-blade {
  position: absolute;
  bottom: 100%;
  width: 3px;
  height: 15px;
  background: linear-gradient(180deg, #7CB87C, #8FBC8F);
  border-radius: 50% 50% 0 0;
  transform-origin: bottom;
  animation: sway 3s infinite ease-in-out;
}

.night-mode .grass-blade {
  background: linear-gradient(180deg, #2a4a3a, #1a3a2a);
}

.grass-blade:nth-child(odd) {
  animation-delay: 0.5s;
}

@keyframes sway {
  0%, 100% { transform: rotate(-5deg); }
  50% { transform: rotate(5deg); }
}

/* 花朵 */
.flowers {
  position: absolute;
  bottom: 5%;
  left: 0;
  right: 0;
  height: 80px;
}

.flower {
  position: absolute;
  bottom: 0;
}

.stem {
  width: 2px;
  height: 25px;
  background: #7CB87C;
  margin: 0 auto;
}

.petals {
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, #FFB6C1, #FF69B4);
  border-radius: 50%;
  margin-bottom: -5px;
  position: relative;
}

.petals::before,
.petals::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, #FFB6C1, #FF69B4);
  border-radius: 50%;
}

.petals::before {
  top: -10px;
  left: 0;
}

.petals::after {
  top: 5px;
  left: -10px;
}

.flower-1 { left: 8%; }
.flower-2 { left: 22%; }
.flower-3 { left: 45%; }
.flower-4 { left: 68%; }
.flower-5 { left: 85%; }
.flower-6 { left: 12%; transform: scale(0.8); }
.flower-7 { left: 55%; transform: scale(0.9); }
.flower-8 { left: 78%; transform: scale(0.7); }

.night-mode .stem {
  background: #3a5a4a;
}

.night-mode .petals,
.night-mode .petals::before,
.night-mode .petals::after {
  background: radial-gradient(circle, #8a6a8a, #6a4a6a);
  opacity: 0.7;
}

/* 栅栏 */
.fence {
  position: absolute;
  bottom: 12%;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 60px;
}

.fence-post {
  position: absolute;
  width: 8px;
  height: 50px;
  background: linear-gradient(90deg, #D4A574, #C49A6C);
  border-radius: 2px;
  top: 0;
}

.fence-post:nth-child(1) { left: 0%; }
.fence-post:nth-child(2) { left: 9%; }
.fence-post:nth-child(3) { left: 18%; }
.fence-post:nth-child(4) { left: 27%; }
.fence-post:nth-child(5) { left: 36%; }
.fence-post:nth-child(6) { left: 45%; }
.fence-post:nth-child(7) { left: 54%; }
.fence-post:nth-child(8) { left: 63%; }
.fence-post:nth-child(9) { left: 72%; }
.fence-post:nth-child(10) { left: 81%; }
.fence-post:nth-child(11) { left: 90%; }
.fence-post:nth-child(12) { left: 99%; }

.fence-rail {
  position: absolute;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(180deg, #E4B584, #D4A574);
  border-radius: 3px;
}

.rail-top { top: 8px; }
.rail-bottom { top: 28px; }

.night-mode .fence-post {
  background: linear-gradient(90deg, #6a5a4a, #5a4a3a);
}

.night-mode .fence-rail {
  background: linear-gradient(180deg, #7a6a5a, #6a5a4a);
}

/* 小鸟 */
.birds {
  position: absolute;
  top: 20%;
  left: 0;
  right: 0;
  height: 100px;
}

.bird {
  position: absolute;
  width: 20px;
  height: 10px;
  border-top: 2px solid #666;
  border-radius: 50% 50% 0 0;
  animation: fly 15s infinite linear;
}

.bird::before,
.bird::after {
  content: '';
  position: absolute;
  width: 10px;
  height: 8px;
  border-top: 2px solid #666;
  border-radius: 50% 50% 0 0;
}

.bird::before {
  left: -8px;
  transform: rotate(-20deg);
}

.bird::after {
  right: -8px;
  transform: rotate(20deg);
}

.bird-1 {
  top: 20px;
  left: -30px;
  animation-duration: 20s;
}

.bird-2 {
  top: 50px;
  left: -50px;
  animation-duration: 25s;
  animation-delay: 5s;
  transform: scale(0.8);
}

.bird-3 {
  top: 35px;
  left: -40px;
  animation-duration: 18s;
  animation-delay: 10s;
  transform: scale(0.6);
}

.night-mode .bird,
.night-mode .bird::before,
.night-mode .bird::after {
  border-color: rgba(200, 200, 220, 0.5);
}

@keyframes fly {
  0% { transform: translateX(0) translateY(0); }
  25% { transform: translateX(30vw) translateY(-10px); }
  50% { transform: translateX(60vw) translateY(5px); }
  75% { transform: translateX(90vw) translateY(-5px); }
  100% { transform: translateX(110vw) translateY(0); }
}

/* 星星（仅夜晚） */
.stars {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.star {
  position: absolute;
  width: 3px;
  height: 3px;
  background: white;
  border-radius: 50%;
  animation: twinkle 2s infinite ease-in-out;
}

.star-1 { top: 10%; left: 15%; animation-delay: 0s; }
.star-2 { top: 15%; left: 35%; animation-delay: 0.5s; }
.star-3 { top: 8%; left: 55%; animation-delay: 1s; }
.star-4 { top: 20%; left: 75%; animation-delay: 1.5s; }
.star-5 { top: 12%; left: 90%; animation-delay: 0.3s; }
.star-6 { top: 25%; left: 10%; animation-delay: 0.8s; }
.star-7 { top: 5%; left: 25%; animation-delay: 1.2s; }
.star-8 { top: 18%; left: 45%; animation-delay: 0.2s; }
.star-9 { top: 22%; left: 65%; animation-delay: 0.7s; }
.star-10 { top: 6%; left: 80%; animation-delay: 1.3s; }
.star-11 { top: 28%; left: 5%; animation-delay: 0.4s; }
.star-12 { top: 3%; left: 40%; animation-delay: 0.9s; }
.star-13 { top: 16%; left: 60%; animation-delay: 1.6s; }
.star-14 { top: 11%; left: 85%; animation-delay: 0.1s; }
.star-15 { top: 24%; left: 20%; animation-delay: 1.1s; }
.star-16 { top: 7%; left: 50%; animation-delay: 0.6s; }
.star-17 { top: 19%; left: 70%; animation-delay: 1.4s; }
.star-18 { top: 14%; left: 30%; animation-delay: 0.2s; }
.star-19 { top: 26%; left: 95%; animation-delay: 1.7s; }
.star-20 { top: 4%; left: 12%; animation-delay: 0.8s; }
.star-21 { top: 21%; left: 52%; animation-delay: 1.0s; }
.star-22 { top: 9%; left: 68%; animation-delay: 0.3s; }
.star-23 { top: 17%; left: 88%; animation-delay: 1.5s; }
.star-24 { top: 13%; left: 3%; animation-delay: 0.7s; }
.star-25 { top: 27%; left: 42%; animation-delay: 1.2s; }
.star-26 { top: 2%; left: 62%; animation-delay: 0.5s; }
.star-27 { top: 23%; left: 78%; animation-delay: 1.8s; }
.star-28 { top: 10%; left: 22%; animation-delay: 0.4s; }
.star-29 { top: 15%; left: 48%; animation-delay: 1.1s; }
.star-30 { top: 6%; left: 92%; animation-delay: 0.9s; }

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* 萤火虫（仅夜晚） */
.fireflies {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50%;
}

.firefly {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #ADFF2F;
  border-radius: 50%;
  box-shadow: 0 0 10px #ADFF2F, 0 0 20px #ADFF2F;
  animation: firefly-glow 3s infinite ease-in-out, firefly-move 20s infinite linear;
}

.firefly-1 { bottom: 20%; left: 10%; animation-delay: 0s, 0s; }
.firefly-2 { bottom: 35%; left: 25%; animation-delay: 0.5s, 2s; }
.firefly-3 { bottom: 15%; left: 40%; animation-delay: 1s, 4s; }
.firefly-4 { bottom: 40%; left: 55%; animation-delay: 1.5s, 6s; }
.firefly-5 { bottom: 25%; left: 70%; animation-delay: 2s, 8s; }
.firefly-6 { bottom: 30%; left: 85%; animation-delay: 0.3s, 10s; }
.firefly-7 { bottom: 18%; left: 15%; animation-delay: 0.8s, 12s; }
.firefly-8 { bottom: 45%; left: 30%; animation-delay: 1.3s, 14s; }
.firefly-9 { bottom: 22%; left: 60%; animation-delay: 1.8s, 16s; }
.firefly-10 { bottom: 38%; left: 75%; animation-delay: 2.3s, 18s; }
.firefly-11 { bottom: 12%; left: 5%; animation-delay: 0.2s, 1s; }
.firefly-12 { bottom: 48%; left: 45%; animation-delay: 0.7s, 3s; }
.firefly-13 { bottom: 28%; left: 80%; animation-delay: 1.2s, 5s; }
.firefly-14 { bottom: 33%; left: 20%; animation-delay: 1.7s, 7s; }
.firefly-15 { bottom: 16%; left: 90%; animation-delay: 2.2s, 9s; }

@keyframes firefly-glow {
  0%, 100% { opacity: 0.4; box-shadow: 0 0 5px #ADFF2F; }
  50% { opacity: 1; box-shadow: 0 0 15px #ADFF2F, 0 0 30px #ADFF2F; }
}

@keyframes firefly-move {
  0% { transform: translate(0, 0); }
  25% { transform: translate(20px, -30px); }
  50% { transform: translate(-10px, -15px); }
  75% { transform: translate(15px, -25px); }
  100% { transform: translate(0, 0); }
}
</style>
