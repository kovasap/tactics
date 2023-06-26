(ns app.interface.repl
  (:require
    [app.interface.initial-db :refer [initial-db]]
    [app.interface.character-stats :refer [can-view-character-intention?]]))

(can-view-character-intention?
  ((:characters initial-db) "Opponent One")
  ((:characters initial-db) "Tortoise"))
