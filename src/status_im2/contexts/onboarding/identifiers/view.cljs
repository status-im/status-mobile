(ns status-im2.contexts.onboarding.identifiers.view
  (:require [react-native.core :as rn]
            [clojure.string :as string]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.onboarding.identifiers.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.common.carousel.view :as carousel]
            [status-im2.contexts.onboarding.common.carousel.animation :as carousel.animation]))

(def header-text
  [{:text     (i18n/label :t/unique-identifiers)
    :sub-text (i18n/label :t/your-identifiers)}
   {:text     (i18n/label :t/identicon-ring)
    :sub-text (i18n/label :t/identicon-ring-explanation)}
   {:text     (i18n/label :t/chat-key-title)
    :sub-text (i18n/label :t/chat-key-description)}
   {:text     (i18n/label :t/emojihash)
    :sub-text (i18n/label :t/emojihash-description)}])

(defn f-view
  []
  (let [progress             (atom nil)
        paused?              (atom nil)
        {:keys [emoji-hash display-name compressed-key
                public-key]} (rf/sub [:multiaccount])
        {:keys [color]}      (rf/sub [:onboarding-2/profile])
        photo-path           (rf/sub [:chats/photo-path public-key])
        emoji-string         (string/join emoji-hash)]
    (carousel.animation/use-initialize-animation progress paused? true)
    (rn/use-effect
     (fn []
       (carousel.animation/cleanup-animation progress paused?))
     [])
    [:<>
     [background/view true]
     [rn/view {:style style/page-container}
      [carousel/view
       {:animate?    true
        :progress    progress
        :paused?     paused?
        :gesture     :tappable
        :header-text header-text}]
      [rn/view {:style style/content-container}
       [quo/profile-card
        {:profile-picture     photo-path
         :name                display-name
         :hash                compressed-key
         :customization-color color
         :emoji-hash          emoji-string
         :show-emoji-hash?    true
         :show-user-hash?     true
         :card-style          style/card-style}]
       [quo/button
        {:accessibility-label       :skip-identifiers
         :on-press                  #(rf/dispatch [:navigate-to :enable-notifications])
         :override-background-color colors/white-opa-5
         :style                     style/button}
        (i18n/label :t/skip)]]]]))

(defn view [props] [:f> f-view props])
