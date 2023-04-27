(ns status-im2.contexts.onboarding.common.background.view
  (:require [react-native.core :as rn]
            [react-native.blur :as blur]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.common.resources :as resources]
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
   {:style  {:resize-mode :stretch
             :width       content-width}
    :source (resources/get-image :onboarding-illustration)}])

(defonce progress (atom nil))
(defonce paused (atom nil))

(defn f-view
  [dark-overlay?]
  (let [view-id      (rf/sub [:view-id])
        animate?     (not dark-overlay?)
        window-width (rf/sub [:dimensions/window-width])]
    (when animate?
      (carousel.animation/use-initialize-animation progress paused animate?))

    (rn/use-effect
     (fn []
       (reanimated/set-shared-value @paused (not= view-id :intro))
       (fn []
         (when (= view-id :generating-keys)
           (carousel.animation/cleanup-animation progress paused))))
     [view-id])

    [rn/view
     {:style style/background-container}
     [carousel/view
      {:animate?          animate?
       :progress          progress
       :header-text       header-text
       :header-background true
       :background        [background-image (* 4 window-width)]}]
     (when dark-overlay?
       [blur/view
        {:style         style/background-blur-overlay
         :blur-amount   30
         :blur-radius   25
         :blur-type     :transparent
         :overlay-color :transparent}])]))

(defn view [dark-overlay?] [:f> f-view dark-overlay?])

