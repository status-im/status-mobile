(ns status-im.contexts.onboarding.identifiers.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.onboarding.common.carousel.animation :as carousel.animation]
    [status-im.contexts.onboarding.common.carousel.view :as carousel]
    [status-im.contexts.onboarding.identifiers.profile-card.view :as profile-card]
    [status-im.contexts.onboarding.identifiers.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def header-text
  [{:text     (i18n/label :t/unique-identifiers)
    :sub-text (i18n/label :t/your-identifiers)}
   {:text     (i18n/label :t/identicon-ring)
    :sub-text (i18n/label :t/identicon-ring-explanation)}
   {:text     (i18n/label :t/chat-key-title)
    :sub-text (i18n/label :t/chat-key-description)}
   {:text     (i18n/label :t/emojihash)
    :sub-text (i18n/label :t/emojihash-description)}])

(defn- navigate-to-enable-notifications
  []
  (rf/dispatch [:onboarding/navigate-to-enable-notifications]))

(defn f-view
  []
  (let [progress             (atom nil)
        paused?              (atom nil)
        is-dragging?         (atom nil)
        drag-amount          (atom nil)
        window-width         (rf/sub [:dimensions/window-width])
        window-height        (rf/sub [:dimensions/window-height])
        {:keys [emoji-hash display-name compressed-key
                public-key]} (rf/sub [:profile/profile])
        {:keys [color]}      (rf/sub [:onboarding/profile])
        photo-path           (rf/sub [:chats/photo-path public-key])
        emoji-string         (string/join emoji-hash)]
    (carousel.animation/use-initialize-animation progress paused? true is-dragging? drag-amount)
    (rn/use-mount #(carousel.animation/cleanup-animation progress paused?))
    [:<>
     [rn/view {:style style/page-container}
      [carousel/view
       {:animate?     true
        :progress     progress
        :paused?      paused?
        :gesture      :swipeable
        :is-dragging? is-dragging?
        :drag-amount  drag-amount
        :header-text  header-text
        :background   [rn/view
                       {:style (style/carousel-background window-height
                                                          (* (count header-text) window-width))}]}]
      [rn/view
       {:style          style/content-container
        :pointer-events :box-none}
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
         :on-press            navigate-to-enable-notifications
         :container-style     style/button}
        (i18n/label :t/continue)]]]]))

(defn view [props] [:f> f-view props])
