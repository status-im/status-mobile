(ns quo2.components.list-items.channel.view
  (:require [quo2.components.list-items.channel.style :as style]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [quo2.components.avatars.channel-avatar.view :as channel-avatar]
            [quo2.components.markdown.text :as quo.text]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.counter.counter.view :as counter]
            [reagent.core :as reagent]))

(defn- view-internal
  "Options:
   - notification - (nil/:notification/:mention/:mute, default: nil):
     - :notification - Display a grey dot.
     - :mention - Display a counter.
     - :mute - Display a mute icon.
   - locked? - (nil/boolean, default: nil):
     - When true, display a locked icon.
     - When false, display an unlocked icon.
   - mentions-count - (default: nil) - Number of mentions to display in the counter with :mention notification.
   - customization-color - (default: nil) - Community color.
   - emoji - (string, default: nil):
     - Emoji to be displayed on the channel avatar.
     - If blank, initials of the channel name are displayed.
   - name - (string, default: nil) - Channel name.
   - on-press - (function, default: nil) - Function called when the component is pressed.
   - on-long-press - (function, default: nil) - Function called when the component is long pressed.
   - theme - Theme value from with-theme HOC"
  []
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [notification locked? mentions-count customization-color emoji name on-press
                 on-long-press theme]}]
      [rn/pressable
       {:style               (style/container @pressed? customization-color theme)
        :accessibility-label :channel-list-item
        :on-press            on-press
        :on-long-press       on-long-press
        :on-press-in         #(reset! pressed? true)
        :on-press-out        #(reset! pressed? false)}
       [channel-avatar/view
        {:size                :size-32
         :locked?             locked?
         :full-name           name
         :customization-color customization-color
         :emoji               emoji}]
       [quo.text/text
        {:style  (style/label notification theme)
         :weight :medium
         :size   :paragraph-1} (str "# " name)]
       (when-not locked?
         (condp = notification
           :mute         [quo2.icons/icon :i/muted
                          {:color (style/mute-notification-icon-color theme)}]
           :mention      [counter/view
                          {:customization-color customization-color
                           :container-style     (style/counter mentions-count)}
                          mentions-count]
           :notification [quo2.icons/icon :i/notification
                          {:color               (style/mute-notification-icon-color theme)
                           :accessibility-label :unviewed-messages-public}]
           nil))])))

(def view (theme/with-theme view-internal))
