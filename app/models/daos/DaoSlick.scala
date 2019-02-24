package models.daos

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait DaoSlick extends DbTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]

