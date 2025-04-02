(ns status-im.contexts.onboarding.common.background.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.common.background.style :as style]
    [status-im.contexts.onboarding.common.carousel.animation :as carousel.animation]
    [status-im.contexts.onboarding.common.carousel.view :as carousel]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def header-text
  [{:text     (i18n/label :t/own-your-crypto)
    :sub-text (i18n/label :t/use-the-multichain-wallet)}
   {:text     (i18n/label :t/chat-with-friends)
    :sub-text (i18n/label :t/with-full-encryption)}
   {:text     (i18n/label :t/store-your-asets-on-keycard)
    :sub-text (i18n/label :t/be-safe-with-secure-cold-wallet)}])

(defn background-image
  [window-width]
  [rn/view {:style (style/background-image-container (* window-width 3))}
   [rn/image
    {:source (resources/get-image :own-buy-and-swap-your-crypto)
     :style  {:align-self :center :width window-width}}]
   [rn/image
    {:source (resources/get-image :chat-privately-with-friends)
     :style  {:align-self :center :width window-width}}]
   [rn/image
    {:source (resources/get-image :store-your-assets-on-keycard)
     :style  {:align-self :center :width window-width}}]])

(defonce progress (atom nil))
(defonce paused? (atom nil))
(defonce is-dragging? (atom nil))
(defonce drag-amount (atom nil))

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
         (when (= view-id :screen/onboarding.preparing-status)
           (carousel.animation/cleanup-animation progress paused?))))
     [view-id])

    [rn/view
     {:style style/background-container}
     [carousel/view
      {:animate?          animate?
       :progress          progress
       :paused?           paused?
       :header-text       header-text
       :is-dragging?      is-dragging?
       :drag-amount       drag-amount
       :header-background true
       :gesture           :swipeable
       :background        [background-image window-width]}]
     (when dark-overlay?
       [quo/blur
        {:style         style/background-blur-overlay
         :blur-amount   (if platform/android? 30 20)
         :blur-radius   (if platform/android? 25 10)
         :blur-type     :transparent
         :overlay-color :transparent}])]))
