(ns status-im2.contexts.onboarding.common.intro.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.common.intro.style :as style]
            [status-im2.common.resources :as resources]))

(def carousels
  [{:image    (resources/get-image :intro-1)
    :text     (i18n/label :t/join-decentralised-communities)
    :sub-text (i18n/label :t/participate-in-the-metaverse)}
   {:image    (resources/get-image :intro-2)
    :text     (i18n/label :t/chat-with-friends)
    :sub-text (i18n/label :t/with-full-encryption)}
   {:image    (resources/get-image :intro-3)
    :text     (i18n/label :t/own-your-crypto)
    :sub-text (i18n/label :t/use-the-multichain-wallet)}
   {:image    (resources/get-image :intro-4)
    :text     (i18n/label :t/discover-web3)
    :sub-text (i18n/label :t/explore-the-decentralized-web)}])

(defn progress-bar
  [index]
  [rn/view style/progress-bar-container
   [rn/view {:style (style/progress-bar-item index 0 false)}]
   [rn/view {:style (style/progress-bar-item index 1 false)}]
   [rn/view {:style (style/progress-bar-item index 2 false)}]
   [rn/view {:style (style/progress-bar-item index 3 true)}]])

;; TODO: carousel component is to be correctly implemented as quo2 component with animations etc in:
;; https://github.com/status-im/status-mobile/issues/15012
(defn carousel
  [index]
  [rn/view {:style style/carousel}
   [progress-bar index]
   [quo/text
    {:style  style/carousel-text
     :weight :semi-bold
     :size   :heading-2}
    (get-in carousels [index :text])]
   [quo/text
    {:style style/carousel-text
     :size  :paragraph-1}
    (get-in carousels [index :sub-text])]])

(defn set-index
  [old-index]
  (mod (inc old-index) 4))

(defn view
  []
  (reagent/with-let
   [carousel-index (reagent/atom 0)
    interval-id
    (js/setInterval
     #(swap! carousel-index set-index)
     1500)]
   [rn/view {:style style/page-container}
    [carousel @carousel-index]
    [rn/image
     {:style  style/page-image
      :source (get-in carousels [@carousel-index :image])}]
    [quo/drawer-buttons
     {:top-card    {:on-press            (fn []
                                           (rf/dispatch [:navigate-to :new-to-status])
                                           (rf/dispatch [:hide-terms-of-services-opt-in-screen]))
                    :heading             (i18n/label :t/sign-in)
                    :accessibility-label :already-use-status-button}
      :bottom-card {:on-press            (fn []
                                           (rf/dispatch [:navigate-to :new-to-status])
                                           (rf/dispatch [:hide-terms-of-services-opt-in-screen]))
                    :heading             (i18n/label :t/im-new-to-status)
                    :accessibility-label :new-to-status-button}}
     (i18n/label :t/you-already-use-status)
     [quo/text
      {:style style/text-container}
      [quo/text
       {:size   :paragraph-2
        :style  style/plain-text
        :weight :semi-bold}
       (i18n/label :t/by-continuing-you-accept)]
      [quo/text
       {:on-press #(rf/dispatch [:open-modal :privacy-policy])
        :size     :paragraph-2
        :style    style/highlighted-text
        :weight   :semi-bold}
       (i18n/label :t/terms-of-service)]]]]
   (finally
    (js/clearInterval interval-id))))
