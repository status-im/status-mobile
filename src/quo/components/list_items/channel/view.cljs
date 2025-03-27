(ns quo.components.list-items.channel.view
  (:require
    [quo.components.avatars.channel-avatar.view :as channel-avatar]
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as quo.icons]
    [quo.components.list-items.channel.style :as style]
    [quo.components.markdown.text :as quo.text]
    [quo.context]
    [react-native.core :as rn]))

(defn view
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
  [{:keys [notification locked? mentions-count customization-color emoji name on-press on-long-press]}]
  (let [theme                  (quo.context/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/pressable
     {:style               (style/container pressed? customization-color theme)
      :accessibility-label :channel-list-item
      :on-press            on-press
      :on-long-press       on-long-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out}
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
         :mute         [quo.icons/icon :i/muted
                        {:color (style/mute-notification-icon-color theme)}]
         :mention      [counter/view
                        {:customization-color customization-color
                         :container-style     (style/counter mentions-count)}
                        mentions-count]
         :notification [quo.icons/icon :i/notification
                        {:color               (style/mute-notification-icon-color theme)
                         :accessibility-label :unviewed-messages-public}]
         nil))]))
