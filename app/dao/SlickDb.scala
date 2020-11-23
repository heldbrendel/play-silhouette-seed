package dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait SlickDb extends TableDefinitions with HasDatabaseConfigProvider[JdbcProfile] {
}
