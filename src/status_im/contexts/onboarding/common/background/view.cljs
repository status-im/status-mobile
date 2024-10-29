(ns status-im.contexts.onboarding.common.background.view
  (:require
    [oops.core :refer [oget]]
    [quo.core :as quo]
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.common.background.style :as style]
    [status-im.contexts.onboarding.common.carousel.animation :as carousel.animation]
    [status-im.contexts.onboarding.common.carousel.view :as carousel]
    [status-im.contexts.shell.jump-to.state :as shell.state]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def header-text
  [{:text     (i18n/label :t/join-decentralised-communities)
    :sub-text (i18n/label :t/participate-in-the-metaverse)}
   {:text     (i18n/label :t/chat-with-friends)
    :sub-text (i18n/label :t/with-full-encryption)}
   {:text     (i18n/label :t/own-your-crypto)
    :sub-text (i18n/label :t/use-the-multichain-wallet)}
   {:text     (i18n/label :t/discover-web3)
    :sub-text (i18n/label :t/explore-the-decentralized-web)}])

(defn background-image
  [image-view-width]
  [rn/image
   {:resize-mode :stretch
    :style       {:flex  1
                  :width image-view-width}
    :source      (resources/get-image :onboarding-illustration)}])

(defonce progress (atom nil))
(defonce paused? (atom nil))
(defonce is-dragging? (atom nil))
(defonce drag-amount (atom nil))

;; Layout height calculation
;; 1. Always prefer on-layout height over window height, as some devices include status bar height in
;; the window while others do not.
;; https://github.com/status-im/status-mobile/issues/14633#issuecomment-1366191478
;; 2. This preference is unless on-layout is triggered with a random value.
;; https://github.com/status-im/status-mobile/issues/14849
;; To ensure that on-layout height falls within the actual height range, a difference between window
;; height and the maximum possible status bar height is allowed (assumed to be 60, as the Pixel emulator
;; has a height of 52).
(defn store-screen-height
  [evt]
  (let [window-height (or (:height (rn/get-window)) 0)
        height        (or (oget evt "nativeEvent" "layout" "height") 0)]
    (when (and (not= height @shell.state/screen-height)
               (< (Math/abs (- window-height height)) 60))
      (reset! shell.state/screen-height height)
      (async-storage/set-item! :screen-height height))))

(defn view
  [dark-overlay?]
  (let [view-id      (rf/sub [:view-id])
        animate?     (not dark-overlay?)
        window-width (rf/sub [:dimensions/window-width])]
    (when animate?
      (carousel.animation/use-initialize-animation progress paused? animate? is-dragging? drag-amount))

    (rn/use-effect
     (fn []
       (reanimated/set-shared-value @paused? (not= view-id :screen/onboarding.intro))
       (fn []
         (when (= view-id :screen/onboarding.generating-keys)
           (carousel.animation/cleanup-animation progress paused?))))
     [view-id])

    [rn/view
     {:style     style/background-container
      :on-layout store-screen-height}
     [carousel/view
      {:animate?          animate?
       :progress          progress
       :paused?           paused?
       :header-text       header-text
       :is-dragging?      is-dragging?
       :drag-amount       drag-amount
       :header-background true
       :gesture           :swipeable
       :background        [background-image (* 4 window-width)]}]
     (when dark-overlay?
       [quo/blur
        {:style         style/background-blur-overlay
         :blur-amount   (if platform/android? 30 20)
         :blur-radius   (if platform/android? 25 10)
         :blur-type     :transparent
         :overlay-color :transparent}])]))
