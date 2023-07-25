(ns status-im2.contexts.onboarding.identifiers.view
  (:require [react-native.core :as rn]
            [clojure.string :as string]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [status-im2.contexts.onboarding.identifiers.profile-card.view :as profile-card]
            [status-im2.contexts.onboarding.identifiers.style :as style]
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
        is-dragging?         (atom nil)
        drag-amount          (atom nil)
        {:keys [emoji-hash display-name compressed-key
                public-key]} (rf/sub [:profile/profile])
        {:keys [color]}      (rf/sub [:onboarding-2/profile])
        photo-path           (rf/sub [:chats/photo-path public-key])
        emoji-string         (string/join emoji-hash)]
    (carousel.animation/use-initialize-animation progress paused? true is-dragging? drag-amount)
    (rn/use-effect
     (fn []
       (carousel.animation/cleanup-animation progress paused?))
     [])
    [:<>
     [rn/view {:style style/page-container}
      [carousel/view
       {:animate?     true
        :progress     progress
        :paused?      paused?
        :gesture      :tappable
        :is-dragging? is-dragging?
        :drag-amount  drag-amount
        :header-text  header-text}]
      [rn/view {:style style/content-container}
       [profile-card/profile-card
        {:profile-picture     photo-path
         :name                display-name
         :hash                compressed-key
         :emoji-hash          emoji-string
         :customization-color color
         :progress            progress}]
       [quo/button
        {:accessibility-label :skip-identifiers
         :type                :grey
         :background          :blur
         :on-press            #(rf/dispatch [:navigate-to-within-stack
                                             [:enable-notifications :new-to-status]])
         :style               style/button}
        (i18n/label :t/skip)]]]]))

(defn view [props] [:f> f-view props])
