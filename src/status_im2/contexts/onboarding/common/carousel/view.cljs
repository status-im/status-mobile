(ns status-im2.contexts.onboarding.common.carousel.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.navigation :as navigation]
            [react-native.reanimated :as reanimated]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.onboarding.common.carousel.style :as style]
            [status-im2.contexts.onboarding.common.carousel.animation :as animation]))

(def header-text
  [{:text     (i18n/label :t/join-decentralised-communities)
    :sub-text (i18n/label :t/participate-in-the-metaverse)}
   {:text     (i18n/label :t/chat-with-friends)
    :sub-text (i18n/label :t/with-full-encryption)}
   {:text     (i18n/label :t/own-your-crypto)
    :sub-text (i18n/label :t/use-the-multichain-wallet)}
   {:text     (i18n/label :t/discover-web3)
    :sub-text (i18n/label :t/explore-the-decentralized-web)}])

(defn header-text-view
  [index window-width]
  [rn/view {:style (style/header-text-view window-width)}
   [quo/text
    {:style  style/carousel-text
     :weight :semi-bold
     :size   :heading-2}
    (get-in header-text [index :text])]
   [quo/text
    {:style style/carousel-sub-text
     :size  :paragraph-1}
    (get-in header-text [index :sub-text])]])

(defn content-view
  [{:keys [window-width status-bar-height index]}]
  (let [content-width (* 4 window-width)]
    [:<>
     [rn/image
      {:style  (style/background-image content-width)
       :source (resources/get-image :onboarding-illustration)}]
     [rn/view {:style (style/header-container status-bar-height content-width index)}
      (for [index (range 4)]
        ^{:key index}
        [header-text-view index window-width])]]))

(defn progress-bar
  [{:keys [static? progress-bar-width]}]
  [rn/view
   {:style (style/progress-bar progress-bar-width)}
   [rn/view {:style (style/progress-bar-item static? false)}]
   [rn/view {:style (style/progress-bar-item static? false)}]
   [rn/view {:style (style/progress-bar-item static? false)}]
   [rn/view {:style (style/progress-bar-item static? true)}]])

(defn dynamic-progress-bar
  [progress-bar-width animate?]
  [:f>
   (fn []
     (let [width          (animation/dynamic-progress-bar-width progress-bar-width animate?)
           container-view (if animate? reanimated/view rn/view)]
       [container-view {:style (style/dynamic-progress-bar width animate?)}
        [progress-bar
         {:static?            false
          :progress-bar-width progress-bar-width}]]))])

(defn view
  [animate?]
  [:f>
   (fn []
     (let [window-width       (rf/sub [:dimensions/window-width])
           view-id            (rf/sub [:view-id])
           status-bar-height  (:status-bar-height @navigation/constants)
           progress-bar-width (- window-width 40)
           carousel-left      (animation/carousel-left-position window-width animate?)
           container-view     (if animate? reanimated/view rn/view)]
       (when animate?
         (rn/use-effect
          (fn []
            (reanimated/set-shared-value @animation/paused (not= view-id :intro)))
          [view-id]))
       [:<>
        [container-view {:style (style/carousel-container carousel-left animate?)}
         (for [index (range 2)]
           ^{:key index}
           [content-view
            {:window-width      window-width
             :status-bar-height status-bar-height
             :index             index}])]
        [rn/view
         {:style (style/progress-bar-container
                  progress-bar-width
                  status-bar-height)}
         [progress-bar
          {:static?            true
           :progress-bar-width progress-bar-width}]
         [dynamic-progress-bar progress-bar-width animate?]]]))])

