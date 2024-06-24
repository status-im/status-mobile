(ns status-im.contexts.profile.utils
  (:require [clojure.string :as string]))

(defn displayed-name
  [{:keys [name display-name preferred-name alias ens-verified primary-name]}]
  ;; `preferred-name` is our own name otherwise we make sure the `name` is verified and use it
  (let [display-name   (when-not (string/blank? display-name)
                         display-name)
        preferred-name (when-not (string/blank? preferred-name)
                         preferred-name)
        ens-name       (or preferred-name
                           display-name
                           name)]
    (if (or preferred-name (and ens-verified name))
      ens-name
      (or display-name primary-name alias name))))

(defn photo
  [{:keys [images]}]
  (or (:large images)
      (:thumbnail images)
      (first images)))
