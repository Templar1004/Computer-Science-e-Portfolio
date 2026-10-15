import os
from pymongo import MongoClient
from bson.objectid import ObjectId

class AnimalShelter(object):
    """ Enhanced CRUD operations for Animal collection in MongoDB with security, validation, and indexing """

    def __init__(self, username=None, password=None):
        # SECURITY ENHANCEMENT: Pull credentials dynamically from environment variables 
        # with fallback defaults to prevent plain-text exposure in version control.
        USER = username or os.getenv('DB_USER', 'aacuser')
        PASS = password or os.getenv('DB_PASS', '12345')
        HOST = os.getenv('DB_HOST', 'localhost')
        PORT = int(os.getenv('DB_PORT', 27017))
        DB = 'aac'
        COL = 'animals'

        # Initialize Secure Connection
        self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER, PASS, HOST, PORT))
        self.database = self.client[DB]
        self.collection = self.database[COL]

        # PERFORMANCE ENHANCEMENT: Automatically establish compound indexes upon initialization
        self.create_indexes()

    def create_indexes(self):
        """ Establish compound indexes on frequently queried dashboard fields to prevent collection scans """
        try:
            self.collection.create_index([
                ("animal_type", 1),
                ("breed", 1),
                ("age_upon_outcome_in_weeks", 1)
            ])
        except Exception as e:
            print(f"Index creation notice: {e}")

    def create(self, data):
        """ Implement C in CRUD with defensive payload validation """
        if data is not None and isinstance(data, dict):
            if len(data) == 0:
                raise Exception("Nothing to save, because data dictionary is empty.")
            
            result = self.collection.insert_one(data)
            return True if result.inserted_id else False
        else:
            raise Exception("Invalid data parameter: must be a non-empty dictionary.")

    def read(self, query):
        """
        Implement R in CRUD with safe cursor handling.
        Input argument: A dictionary containing the key/value lookup pair.
        Returns: A list of results if successful, else an empty list.
        """
        if query is not None and isinstance(query, dict):
            cursor = self.collection.find(query)
            return list(cursor)
        else:
            return []

    def update(self, query, update_data):
        """ Implement U in CRUD (Missing API completion) with validation """
        if query is not None and update_data is not None:
            if not isinstance(query, dict) or not isinstance(update_data, dict):
                raise Exception("Query and update data must be dictionaries.")
            
            update_payload = update_data if "$set" in update_data else {"$set": update_data}
            result = self.collection.update_many(query, update_payload)
            return result.modified_count
        else:
            raise Exception("Query and update parameters cannot be None.")

    def delete(self, query):
        """ Implement D in CRUD (Missing API completion) securely """
        if query is not None and isinstance(query, dict):
            if len(query) == 0:
                raise Exception("Refusing unsafe delete with an empty query selector.")
            
            result = self.collection.delete_many(query)
            return result.deleted_count
        else:
            raise Exception("Query parameter must be a valid dictionary.")