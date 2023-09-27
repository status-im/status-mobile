(ns ^{:doc "Pairing utils"} status-im.utils.pairing)

(defn has-paired-installations?
  [cofx]
  (let [our-installation-id (get-in cofx [:db :profile/profile :installation-id])]
    (->>
      (get-in cofx [:db :pairing/installations])
      vals
      (some (fn [{:keys [enabled? installation-id]}]
              (and (not= installation-id our-installation-id)
                   enabled?))))))
