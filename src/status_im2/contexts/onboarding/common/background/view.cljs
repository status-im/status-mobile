(ns status-im2.contexts.onboarding.common.background.view
  (:require [react-native.core :as rn]
            [react-native.blur :as blur]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [oops.core :refer [oget]]
            [react-native.platform :as platform]
            [status-im2.common.resources :as resources]
            [react-native.async-storage :as async-storage]
            [status-im2.contexts.shell.jump-to.state :as shell.state]
            [status-im2.contexts.onboarding.common.carousel.view :as carousel]
            [status-im2.contexts.onboarding.common.background.style :as style]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.onboarding.common.carousel.animation :as carousel.animation]))

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
  [content-width]
  [rn/image
   {:style  {:resize-mode   :stretch
             :resize-method :scale
             :margin-top    32
             :width         content-width}
    :source (resources/get-image :onboarding-illustration)}])

(defonce progress (atom nil))
(defonce paused? (atom nil))
(defonce is-dragging? (atom nil))
(defonce drag-amount (atom nil))

(defn store-screen-height
  [evt]
  (let [window-height (:height (rn/get-window))
        height        (or (oget evt "nativeEvent" "layout" "height") 0)
        width         (or (oget evt "nativeEvent" "layout" "width") 0)]
    ;; Layout height calculation
    ;; 1. Make sure height is more than width, and on-layout is not fired while the
    ;; screen is horizontal
    ;; 2. Initialize values with 0 in case of nil
    ;; 3. In the case of notch devices, the dimensions height will be smaller than
    ;; on-layout,
    ;; (without status bar height included)
    ;; https://github.com/status-im/status-mobile/issues/14633
    ;; 4. In the case of devices without a notch, both heights should be the same,
    ;; but actual values differ in some pixels, so arbitrary 5 pixels is allowed
    (when (and (> height width)
               (>= (+ height 5) (or window-height 0))
               (not= height @shell.state/screen-height))
      (reset! shell.state/screen-height height)
      (async-storage/set-item! :screen-height height))))

(defn f-view
  [dark-overlay?]
  (let [view-id      (rf/sub [:view-id])
        animate?     (not dark-overlay?)
        window-width (rf/sub [:dimensions/window-width])]
    (when animate?
      (carousel.animation/use-initialize-animation progress paused? animate? is-dragging? drag-amount))

    (rn/use-effect
     (fn []
       (reanimated/set-shared-value @paused? (not= view-id :intro))
       (fn []
         (when (= view-id :generating-keys)
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
       [blur/view
        {:style         style/background-blur-overlay
         :blur-amount   (if platform/android? 30 20)
         :blur-radius   (if platform/android? 25 10)
         :blur-type     :transparent
         :overlay-color :transparent}])]))

(defn view [dark-overlay?] [:f> f-view dark-overlay?])
