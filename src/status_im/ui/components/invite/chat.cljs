(ns status-im.ui.components.invite.chat
  (:require [clojure.string :as cstr]
            [quo.design-system.colors :as colors]
            [quo.design-system.spacing :as spacing]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.ui.components.invite.utils :as utils]
            [quo.core :as quo]
            [status-im.acquisition.chat :as acquisition]
            [status-im.ui.components.invite.events :as invite]
            [status-im.i18n.i18n :as i18n]
            [status-im.acquisition.gateway :as gateway]
            [status-im.ui.components.invite.style :as styles]))

(defn messages-wrapper []
  (:small spacing/padding-vertical))

(defn message-wrapper []
  {:padding-right  96
   :padding-left   8
   :flex-direction :row})

(defn message-view []
  {:border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-right-radius 16
   :border-radius              8
   :border-bottom-left-radius  4
   :border-width               1
   :margin-bottom              4
   :border-color               (:border-01 @colors/theme)})

(defn message-text []
  (merge (:small spacing/padding-horizontal)
         {:padding-vertical 6}))

(defn author-text []
  (merge (:small spacing/padding-horizontal)
         {:padding-top   (:tiny spacing/spacing)
          :paddingbottom (:x-tiny spacing/spacing)}))

(defn button-message []
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(defn starter-pack-style []
  (merge (:small spacing/padding-horizontal)
         (:small spacing/padding-vertical)
         {:background-color    (:interactive-02 @colors/theme)
          :flex-direction      :row
          :border-bottom-width 1
          :border-top-width    1
          :border-color        (:border-02 @colors/theme)}))

(defn starter-pack []
  (let [pack        @(re-frame/subscribe [::invite/starter-pack])
        tokens      (utils/transform-tokens pack)
        reward-text (->> tokens
                         (map (comp :symbol first))
                         (filter (comp not nil?))
                         (map name)
                         (cstr/join ", "))]
    [rn/view {:style (starter-pack-style)}
     [rn/view {:style (styles/reward-tokens-icons (count tokens))}
      (doall
       (for [[{name             :name
               {source :source} :icon} _ idx] tokens]
         ^{:key name}
         [rn/view {:style (styles/reward-token-icon idx)}
          [rn/image {:source (if (fn? source) (source) source)
                     :style  {:width  40
                              :height 40}}]]))]
     [rn/view {:style styles/reward-description}
      [quo/text {:weight :medium}
       (i18n/label :t/invite-chat-starter-pack)]
      [quo/text {} reward-text]]]))

(defn render-message [{:keys [content]}]
  [rn/view {:style (message-wrapper)}
   [rn/view {:style (message-view)}
    (for [{:keys [type value]} content]
      (case type
        :text   [rn/view {:style (message-text)}
                 [quo/text value]]
        :pack   [starter-pack]
        :author [rn/view {:style (author-text)}
                 [quo/text {:color :secondary}
                  value]]
        :button [rn/view {:style (button-message)}
                 value]
        nil))]])

(defn reward-messages []
  (let [has-invite @(re-frame/subscribe [::invite/has-chat-invite])
        loading    (#{(get gateway/network-statuses :initiated)
                      (get gateway/network-statuses :in-flight)}
                    @(re-frame/subscribe [::gateway/network-status]))
        pending    @(re-frame/subscribe [::invite/pending-reward])
        messages   [{:content [{:type  :text
                                :value "👋"}]}
                    {:content [{:type  :author
                                :value (i18n/label :t/invite-chat-name)}
                               {:type  :text
                                :value (i18n/label :t/invite-chat-intro)}
                               {:type :pack}
                               {:type  :button
                                :value [quo/button {:type     :secondary
                                                    :loading  loading
                                                    :disabled pending
                                                    :on-press #(re-frame/dispatch [::acquisition/accept-pack])}
                                        (if pending
                                          (i18n/label :t/invite-chat-pending)
                                          (i18n/label :t/invite-chat-accept))]}]}
                    {:content [{:type  :text
                                :value (i18n/label :t/invite-chat-rule)}]}
                    {:content [{:type  :text
                                :value [:<>
                                        (i18n/label :t/invite-privacy-policy1) " "
                                        [quo/text {:color    :link
                                                   :on-press #(re-frame/dispatch [::invite/terms-and-conditions])}
                                         (i18n/label :t/invite-privacy-policy2)]]}]}]]
    (when has-invite
      [rn/view {:style (messages-wrapper)}
       (for [message messages]
         [render-message message])])))
